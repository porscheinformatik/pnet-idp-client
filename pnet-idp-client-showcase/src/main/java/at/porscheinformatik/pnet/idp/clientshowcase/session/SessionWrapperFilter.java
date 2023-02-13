package at.porscheinformatik.pnet.idp.clientshowcase.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class SessionWrapperFilter extends OncePerRequestFilter
{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        filterChain.doFilter(new SerializingSessionHttpServletRequestWrapper(request), response);
    }

    @Override
    protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException
    {
        doFilterInternal(request, response, filterChain);
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch()
    {
        return false;
    }

    private static final class SerializingSessionHttpServletRequestWrapper extends HttpServletRequestWrapper
    {
        private SerializingSessionWrapper wrapper;

        public SerializingSessionHttpServletRequestWrapper(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public HttpSession getSession(boolean create)
        {
            if (wrapper == null)
            {
                HttpSession delegateSession = super.getSession(create);

                if (delegateSession != null)
                {
                    wrapper = new SerializingSessionWrapper(delegateSession);
                }
            }

            return wrapper;
        }

        @Override
        public HttpSession getSession()
        {
            return getSession(true);
        }
    }

    private static final class SerializingSessionWrapper implements HttpSession
    {
        private final HttpSession delegate;

        private boolean invalid;

        public SerializingSessionWrapper(HttpSession delegate)
        {
            super();

            this.delegate = delegate;
        }

        @Override
        public Object getAttribute(String name)
        {
            Object serialized = delegate.getAttribute(name);

            if (serialized == null)
            {
                return null;
            }

            try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream((byte[]) serialized));)
            {
                return stream.readObject();
            }
            catch (IOException | ClassNotFoundException e)
            {
                throw new RuntimeException("Error deserializing session object", e);
            }
        }

        @Override
        public void setAttribute(String name, Object value)
        {
            if (value == null)
            {
                delegate.setAttribute(name, value);
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (ObjectOutputStream stream = new ObjectOutputStream(bytes))
            {
                stream.writeObject(value);
                stream.flush();

                delegate.setAttribute(name, bytes.toByteArray());
            }
            catch (IOException e)
            {
                throw new RuntimeException(String.format("Error serializing session object %s", value), e);
            }
        }

        @Override
        public long getCreationTime()
        {
            return delegate.getCreationTime();
        }

        @Override
        public String getId()
        {
            return delegate.getId();
        }

        @Override
        public long getLastAccessedTime()
        {
            return delegate.getLastAccessedTime();
        }

        @Override
        public ServletContext getServletContext()
        {
            return delegate.getServletContext();
        }

        @Override
        public void setMaxInactiveInterval(int interval)
        {
            delegate.setMaxInactiveInterval(interval);
        }

        @Override
        public int getMaxInactiveInterval()
        {
            return delegate.getMaxInactiveInterval();
        }

        @Override
        public Enumeration<String> getAttributeNames()
        {
            return delegate.getAttributeNames();
        }

        @Override
        public void removeAttribute(String name)
        {
            if (!invalid)
            {
                delegate.removeAttribute(name);
            }
        }

        @Override
        public void invalidate()
        {
            delegate.invalidate();

            this.invalid = true;
        }

        @Override
        public boolean isNew()
        {
            return delegate.isNew();
        }

    }
}
