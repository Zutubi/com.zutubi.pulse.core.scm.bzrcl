package com.zutubi.pulse.core.scm.bzrcl;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

/**
 * Subversion command-line-based SCM configuration.
 */
@Form(fieldOrder = { "url", "username", "password", "checkoutScheme", "filterPaths", "externalMonitorPaths", "verifyExternals", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@SymbolicName("zutubi.bzrConfig")
public class BzrConfiguration extends PollableScmConfiguration
{
    @Required
    private String url;
    @Required
    private String username;
    @Password
    private String password;
    @Wizard.Ignore
    @Numeric(min = 0)
    private int inactivityTimeout;
    
    public BzrConfiguration()
    {
    }

    public BzrConfiguration(String url, String name, String password, int inactivityTimeout)
    {
        this.url = url;
        this.username = name;
        this.password = password;
        this.inactivityTimeout = inactivityTimeout;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getType()
    {
        return "bzrcl";
    }

	public int getInactivityTimeout()
	{
		return inactivityTimeout;
	}

	public void setInactivityTimeout(int inactivityTimeout)
	{
		this.inactivityTimeout = inactivityTimeout;
	}
}
