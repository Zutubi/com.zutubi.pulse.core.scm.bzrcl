package com.zutubi.pulse.core.scm.bzrcl;

import com.zutubi.pulse.core.scm.bzrcl.BzrClient;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;

/**
 * A factory for creating bzr clients from bzr config.
 */
public class BzrClientFactory implements ScmClientFactory<BzrConfiguration>
{
    @Override
    public ScmClient createClient(BzrConfiguration config) throws ScmException
    {
        return new BzrClient(config);
    }
}
