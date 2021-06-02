/**
 *
 */
package at.porscheinformatik.idp;

import java.io.Serializable;

/**
 * @author Daniel Furtlehner
 */
public abstract class AbstractCompanyDependentClaim implements Serializable
{
    private static final long serialVersionUID = 6859100546707295055L;

    private final Integer companyId;

    public AbstractCompanyDependentClaim(Integer companyId)
    {
        super();

        this.companyId = companyId;
    }

    public Integer getCompanyId()
    {
        return companyId;
    }
}
