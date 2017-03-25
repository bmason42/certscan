package org.mason.certscan.api.model;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by masonb on 3/25/2017.
 */
@XmlRootElement
@ApiModel("Version for the application")
public class About {
    public String version="1.0";
}
