package org.edx.mobile.tta.wordpress_client.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JARVICE on 28-12-2017.
 */

public class WPProfileModel {
    public Long id;
    public String name;
    public String username;
    public String email;
    public List<String> roles;
}