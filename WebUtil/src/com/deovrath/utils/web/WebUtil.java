package com.deovrath.utils.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.deovrath.utils.Logger.InOutError;

public class WebUtil 
{
	
	public String sha256_hex_string(String text) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(text.getBytes("utf-8"));
        return(bytesToHex(encodedHash));
    }

    private static String bytesToHex(byte[] hash)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++)
        {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public String executeRestWebService(String mUrl, String requestMethod, String body, Map<String,String> requestHeaders, InOutError error)
    {
        HttpURLConnection con = null;
        OutputStream requestStream = null;
        InputStream responseStream = null;

        try
        {
            URL url = new URL(mUrl);

            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("User-Agent","Apache-HttpClient/4.1.1 (java 1.5)");
            
            if(requestHeaders!=null)
            {
            		for(Map.Entry<String,String> entry : requestHeaders.entrySet())
            		{
            			con.setRequestProperty(entry.getKey(), entry.getValue());
            		}
            }
            	
            con.connect();
            
            if(body!=null)
            {
            		requestStream = con.getOutputStream();
            		byte[] data = body.getBytes("utf-8");
            		requestStream.write(data);
            		requestStream.flush();
            }

            responseStream = con.getInputStream();

            StringWriter writer = new StringWriter();
            IOUtils.copy(responseStream, writer,"utf-8");

            return(writer.toString());
        }
        catch(Exception ex)
        {
            error.setError(ex.getMessage());
            return(null);
        }
        finally
        {
            try
            {
                if (responseStream != null) {
                    responseStream.close();
                }
                if (requestStream != null) {
                    requestStream.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            }
            catch (IOException ioex)
            {
                // do nothing
            }
        }
    }
	
    public String executeSoapWebService(String mUrl, String soapMsg, String soapAction, Map<String,String> requestHeaders, InOutError error)
    {
        HttpURLConnection con = null;
        OutputStream requestStream = null;
        InputStream responseStream = null;

        try
        {
            URL url = new URL(mUrl);

            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);

            byte[] data = soapMsg.getBytes("utf-8");
            
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", "text/xml; charset=utf-8");
            con.setRequestProperty("SOAPAction",soapAction);
            con.setRequestProperty("Content-Length",String.valueOf(data.length));
            con.setRequestProperty("User-Agent","Apache-HttpClient/4.1.1 (java 1.5)");
            
            if(requestHeaders!=null)
            {
            		for(Map.Entry<String,String> entry : requestHeaders.entrySet())
            		{
            			con.setRequestProperty(entry.getKey(), entry.getValue());
            		}
            }

            requestStream = con.getOutputStream();
            requestStream.write(data);
            requestStream.flush();

            responseStream = con.getInputStream();

            StringWriter writer = new StringWriter();
            IOUtils.copy(responseStream, writer,"utf-8");

            return(writer.toString());
        }
        catch(Exception ex)
        {
            error.setError(ex.getMessage());
            return(null);
        }
        finally
        {
            try
            {
                if (responseStream != null) {
                    responseStream.close();
                }
                if (requestStream != null) {
                    requestStream.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            }
            catch (IOException ioex)
            {
                // do nothing
            }
        }
    }
}
