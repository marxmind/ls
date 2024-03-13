package com.italia.municipality.lakesebu.xml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/19/2019
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RCDFormDetails {

	private String formId="";
	private String name="";
	private String seriesFrom="";
	private String seriesTo="";
	private String amount="";
	private String style;
	private String stabNo;
}