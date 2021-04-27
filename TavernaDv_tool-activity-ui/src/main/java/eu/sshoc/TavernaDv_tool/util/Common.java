/*******************************************************************************
 * Copyright 2018 VRE4EIC Consortium
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package eu.sshoc.TavernaDv_tool.util;


import java.net.MalformedURLException;
import java.net.URL;
/**
 * This class contain the sensible information to save a workflow in a dataset in dataverse
 */
public class Common {
	
	private static String token;
	private static String userId;
	private static String password;
	private static String dsName;
	private static String destinationPath = null;	
	private static URL dVUrl;
	
	
	public Common() {
		//token="";
	}
	
	public static URL getLoginUrl() throws MalformedURLException {
		return new URL(dVUrl+"/path/login?username="+userId+"&pwd="+password);
	}

	public static URL getEvreUrl() {
		return dVUrl;
	}

	public static void setEvreUrl(URL evreUrl) {
		Common.dVUrl = evreUrl;
	}
	public static String getUserId() {
		return userId;
	}

	public static void setUserId(String userId) {
		Common.userId = userId;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		Common.password = password;
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		Common.token = token;
	}

	public static String getDsName() {
		return dsName;
	}

	public static void setDsName(String dsName) {
		Common.dsName = dsName;
	}

	public static String getDestinationPath() {
		return destinationPath;
	}

	public static void setDestinationPath(String destinationPath) {
		Common.destinationPath = destinationPath;
	}

}
