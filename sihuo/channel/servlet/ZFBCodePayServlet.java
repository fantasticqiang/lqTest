package com.channel.servlet;

import cn.phoneposp.dao.AliTradeInfoDAO;
import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.AliTradeInfo;
import cn.phoneposp.entity.Merchant;
import cn.phoneposp.entity.RBgroup;
import cn.phoneposp.entity.RBgrouptradvalue;
import cn.phoneposp.entity.RPscalecommission;
import cn.phoneposp.entity.RWZFBConsumer;
import com.channel.util.MD5;
import com.channel.util.SignUtils;
import com.channel.util.XmlUtils;
import com.common.util.AmountUtil;
import com.interfaces.O110101.util.DateUtil;
import com.phoneposp.util.PropertiesUtil;
import com.phoneposp.util.SequenceUtils;

import dao.MerchantDao;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class ZFBCodePayServlet
  extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  Logger logger = Logger.getLogger(ZFBCodePayServlet.class);
  public PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class
    .getResource("/merchantInfo.properties").getFile());
  public String prefix_al = this.util.getValue("prefix_al");
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    doPost(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    request.setCharacterEncoding("utf-8");
    response.setCharacterEncoding("utf-8");
    response.setContentType("text/html;charset=utf-8");
    this.logger.info("==============支付宝生成二维码开始=============");
    Map<String, String> reqmap = new HashMap<String,String>();
    SortedMap<String, String> map = new TreeMap<String,String>();
	MerchantDao merchantDao = new MerchantDao();
	
	List<Merchant> merchantList = merchantDao.getWXMerchant();//获取商户号
	if (merchantList.size() == 1) {
		merchantDao.updateWXMerchant();
	}
	boolean bl = true;
	for (int i = 0; i < merchantList.size(); i++) {
		Merchant merchant = merchantList.get(i);
		if (merchantDao.queryByMerchantNo(merchant.getMerchantNo())) {
			bl = false;
			String mch_id = merchant.getMerchantNo();//获取该笔订单的商户号
		
    String notify_url = "http://shuaka.qingyutec.com/PhonePospInterface/ZFBCodeCallBackServlet";
    String key = "9d101c97133837e13dde2d32a5054abb";
    String saruLruid = request.getParameter("saruLruid");
    String type = request.getParameter("type");
    new SaruInfoDao();Integer id = SaruInfoDao.getSequence("SEQ_ORDER_NO");
    String total_fee = request.getParameter("transAmt");
    String mchIp = getIp(request);
    
    String orderNum = this.prefix_al + 
      SequenceUtils.createSequence(id.intValue(), new int[] { 1, 7, 1, 6, 3, 
      3, 9, 8, 5, 6, 0, 5 }, new int[][] { { 2, 12 }, {
      5, 9 }, { 3, 6 }, { 7, 9 } });
    map.put("service", "pay.alipay.native");
    map.put("mch_id", mch_id);
    map.put("out_trade_no", orderNum);
    map.put("body", "刷喀支付");
    map.put("total_fee", total_fee);
    map.put("notify_url", notify_url);
    map.put("mch_create_ip", mchIp);
    map.put("nonce_str", String.valueOf(new Date().getTime()));
    
    Map<String, String> params = SignUtils.paraFilter(map);
    StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
    
    SignUtils.buildPayParams(buf, params, false);
    String preStr = buf.toString();
    
    String sign = MD5.sign(preStr, "&key=" + key, "utf-8");
    map.put("sign", sign);
    String reqUrl = "https://pay.swiftpass.cn/pay/gateway";
    System.out.println("reqUrl：" + reqUrl);
    
    System.out.println("reqParams:" + XmlUtils.parseXML(map));
    CloseableHttpResponse resp = null;
    CloseableHttpClient client = null;
    String res = null;
    try
    {
      HttpPost httpPost = new HttpPost(reqUrl);
      StringEntity entityParams = new StringEntity(
        XmlUtils.parseXML(map), "utf-8");
      httpPost.setEntity(entityParams);
      httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
      client = HttpClients.createDefault();
      resp = client.execute(httpPost);
      if ((resp != null) && (resp.getEntity() != null))
      {
        Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
        res = XmlUtils.toXml(resultMap);
        System.out.println("请求结果：" + res);
        if (("0".equals(resultMap.get("status"))) && ("0".equals(resultMap.get("result_code"))))
        {
          SaruInfoDao infoDao = new SaruInfoDao();
          AliTradeInfoDAO aliDAO = new AliTradeInfoDAO();
          
          RPscalecommission scalecommission = infoDao.querySracT0Rate(saruLruid);
          RBgroup rbGroup = infoDao.getSarugroupBySaruLruid(saruLruid);
          RBgrouptradvalue rbGroupTradeValue = infoDao.querySracAliAgentSettlePrice(saruLruid);
          double rate = scalecommission.getAlipayvalue();
          double totalFee = AmountUtil.div(Double.parseDouble(total_fee), 100.0D, 2);
          double handFee = AmountUtil.ceiling(AmountUtil.mul(totalFee, rate), 2);
          double channelMoney = AmountUtil.ceiling(AmountUtil.mul(totalFee, 0.0018D), 2);
          double rugrMoney = AmountUtil.sub(handFee, AmountUtil.mul(totalFee, rbGroupTradeValue.getAlipyvalue()));
          RWZFBConsumer rwZFBConsumer = new RWZFBConsumer();
          rwZFBConsumer.setBcconOrdernum(orderNum);
          rwZFBConsumer.setBcconMoney(totalFee);
          rwZFBConsumer.setBcconSalemoney(AmountUtil.sub(totalFee, handFee));
          rwZFBConsumer.setBcconCommission(handFee);
          rwZFBConsumer.setBcconArgedate(DateUtil.getYYYYmmddHHmmss());
          rwZFBConsumer.setSaruLruid(saruLruid);
          rwZFBConsumer.setBcconState("WAIT");
          rwZFBConsumer.setChruInterface("");
          rwZFBConsumer.setBcconChannelmoney(channelMoney);
          rwZFBConsumer.setBcconGroup(rbGroup.getRugr_id()+"");
          rwZFBConsumer.setBcconBelongGroup(rbGroup.getSaru_rugrid2());
          rwZFBConsumer.setBcconRugrMoney(rugrMoney);
          rwZFBConsumer.setBcconRebate("1");
          aliDAO.inserRWZFBComsumer(rwZFBConsumer);
          
          AliTradeInfo aliInfo = new AliTradeInfo();
          aliInfo.setOut_trade_no(orderNum);
          aliInfo.setTotal_amount(totalFee);
          aliInfo.setOperator_id(saruLruid);
          aliInfo.setTime_expire(DateUtil.getYYYYmmddHHmmss());
          aliInfo.setGmt_create(DateUtil.getYYYYmmddHHmmss());
          aliInfo.setTrade_status("WAIT_BUYER_PAY");
          aliDAO.insertAliTradeInfo(aliInfo);
          
          reqmap.put("resultCode", "0");
          reqmap.put("requestNo", orderNum);
          reqmap.put("code_url", (String)resultMap.get("code_url"));
          reqmap.put("errorMsg", "获取二维码成功");
        }
        else
        {
        	reqmap.put("resultCode", "-99996");
        	reqmap.put("errorMsg", "获取二维码失败," + (String)resultMap.get("message"));
        }
      }
      else
      {
    	  reqmap.put("resultCode", "-99999");
    	  reqmap.put("errorMsg", "获取二维码失败");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
		}
		break;
	}
	if (bl) {
		reqmap.put("resultCode", "-99997");
		reqmap.put("errorMsg", "当天交易额度已满");
	}
	
    JSONObject json = JSONObject.fromObject(reqmap);
	response.getWriter().write(json.toString());
	response.getWriter().flush();
	response.getWriter().close();
	logger.info("==============支付宝生成二维码结束=============");
    
    
  }
  
  private String getIp(HttpServletRequest request)
  {
    String ipAddress = null;
    ipAddress = request.getHeader("x-forwarded-for");
    if ((ipAddress == null) || (ipAddress.length() == 0) || 
      ("unknown".equalsIgnoreCase(ipAddress))) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if ((ipAddress == null) || (ipAddress.length() == 0) || 
      ("unknown".equalsIgnoreCase(ipAddress))) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if ((ipAddress == null) || (ipAddress.length() == 0) || 
      ("unknown".equalsIgnoreCase(ipAddress)))
    {
      ipAddress = request.getRemoteAddr();
      if (ipAddress.equals("127.0.0.1"))
      {
        InetAddress inet = null;
        try
        {
          inet = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
          e.printStackTrace();
        }
        ipAddress = inet.getHostAddress();
      }
    }
    if ((ipAddress != null) && (ipAddress.length() > 15)) {
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    return ipAddress;
  }
}
