/**
 * Project Name:pay-protocol
 * File Name:Xml.java
 * Package Name:cn.swiftpass.pay.protocol
 * Date:2014-8-10下午10:48:21
 *
*/

package com.channel.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.util.BufferRecycler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;


/**
 * ClassName:Xml
 * Function: XML的工具方法
 * Date:     2014-8-10 下午10:48:21 
 * @author    
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class XmlUtils {
    
    /** <一句话功能简述>
     * <功能详细描述>request转字符串
     * @param request
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String parseRequst(HttpServletRequest request){
        String body = "";
        try {
            ServletInputStream inputStream = request.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while(true){  // 因此while(true) 是一个无限循环，因为表达式的值一直为真。为了跳出循环，循环体内部要用break语句来跳出。
                String info = br.readLine();
                if(info == null){      // readLine()方法读出是null，就表示文件结尾了。
                    break;
                }
                if(body == null || "".equals(body)){
                    body = info;
                }else{
                    body += info;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }            
        return body;
    }

    
    public static String parseXML(SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if (null != v && !"".equals(v) && !"appkey".equals(k)) {
                sb.append("<" + k + ">" + parameters.get(k) + "</" + k + ">\n");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 从request中获得参数Map，并返回可读的Map
     * 
     * @param request
     * @return
     */
    public static SortedMap getParameterMap(HttpServletRequest request) {
        // 参数Map
        Map properties = request.getParameterMap();
        // 返回值Map
        SortedMap returnMap = new TreeMap();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if(null == valueObj){
                value = "";
            }else if(valueObj instanceof String[]){
                String[] values = (String[])valueObj;
                for(int i=0;i<values.length;i++){
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length()-1);
            }else{
                value = valueObj.toString();
            }
            returnMap.put(name, value.trim());
        }
        returnMap.remove("method");
        return returnMap;
    }
    
    /**
     * 转XMLmap
     * @author  
     * @param xmlBytes
     * @param charset
     * @return
     * @throws Exception
     */
    public static Map<String, String> toMap(byte[] xmlBytes,String charset) throws Exception{
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(xmlBytes));
        source.setEncoding(charset);
        Document doc = reader.read(source);
        Map<String, String> params = XmlUtils.toMap(doc.getRootElement());
        return params;
    }
    
    /**
     * 转MAP
     * @author  
     * @param element
     * @return
     */
    public static Map<String, String> toMap(Element element){
        Map<String, String> rest = new HashMap<String, String>();
        List<Element> els = element.elements();
        for(Element el : els){
            rest.put(el.getName().toLowerCase(), el.getTextTrim());
        }
        return rest;
    }
    
    public static String toXml(Map<String, String> params){
        StringBuilder buf = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        buf.append("<xml>");
        for(String key : keys){
            buf.append("<").append(key).append(">");
            buf.append("<![CDATA[").append(params.get(key)).append("]]>");
            buf.append("</").append(key).append(">\n");
        }
        buf.append("</xml>");
        return buf.toString();
    }
    //测试
    public static void main(String[] args) {
//		SAXReader reader = new SAXReader();
		try {
//			File file = new File("C:\\Users\\houlei\\Desktop\\新建文件夹\\web.xml");
//			BufferedReader in = new BufferedReader(new FileReader(file));
//			String str=null;
//			while((str =in.readLine()) !=null){
//                System.out.println(str);
//			}
//			System.out.println(System.currentTimeMillis()+"".length());
			
//			Document document =reader.read(new File("C:\\Users\\houlei\\Desktop\\新建文件夹\\web.xml"));
//			Element element =document.getRootElement();
//			List<Element> elements =element.elements();
//			for(Element el:elements){
//				System.out.println(el.getName());
//				System.out.println(el.getText());
//			}
			
			File file = new File("C:\\Users\\houlei\\Desktop\\res.xml");
			Map<String,String> map = new HashMap<String,String>();
			String strXml = FileUtils.readFileToString(file, "utf-8");
			String xm=removeSpecialXMLChar(strXml);
			
			System.out.println("--------------"+xm);
			Document doc = DocumentHelper.parseText(strXml);
			Element root= doc.getRootElement();
			List<Element> es= root.elements();
			for(Element e:es){
				map.put(e.getName().toLowerCase(), e.getTextTrim());
				
			}
			String str = map.get("pay_info");
			JSONObject json = JSONObject.fromObject(str);
			System.out.println("========"+json.optString("status"));
			System.out.println("==========================="+map.get("pay_info"));
			
			StringBuffer sb = new StringBuffer("");
			sb.append("<xml>");
			Set<Entry<String,String>> entrySet = map.entrySet();
			for(Entry<String,String> e:entrySet){
				System.out.println("Key: "+e.getKey()+" value:"+e.getValue());
				sb.append("<").append(e.getKey()).append(">").append(e.getValue()).append("</").append(e.getKey()).append(">");
			}
			sb.append("</xml>");
			System.out.println(sb.toString());
			
//			Set<String> set = map.keySet();
//			for(String s:set){
//				System.out.println("key: "+s+" value"+map.get(s));
//			}
//			for (int i = 0; i < set.size(); i++) {
////				System.out.println("key "+set.+" value"+map.get(key));
//			}
//			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public static String removeSpecialXMLChar(String src) {
      
        String rs = src.replace("<![CDATA[", "{");
        rs = rs.replace("]]>", "}");
        rs = rs.replace(">", "》");
        rs = rs.replace("<", "《");
        rs = rs.replace("&", "$");
        rs = rs.replace("|", ":");
        return rs;
  }

}

