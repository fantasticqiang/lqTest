package com.syf.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerchantEnterModel;
import com.syf.util.Commons;
import com.syf.util.HttpMsg;
import com.syf.util.HttpUtil;

public class RegisteTest {

	public static void main(String[] args) {
		// 获取App前台参数
		String saruLruid = "6000002332";// 商户号
		String reservedPhone = "15601205173"; // 结算卡绑定的手机号
		String idCard = "110101198702201514"; // 身份证号
		String cardNo = "6214830125502616";//卡号
		
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		MerchantEnterModel merchantInfo = merchantDao.queryMerchantBysaruId(saruLruid);
		//查询城市，地区id
		String provinceArgs =  merchantInfo.getProvinceId(); // 省份
		String cityArgs =  merchantInfo.getCityId(); // 城市
		
		System.out.println("省份"+provinceArgs);
		
		/** 请求参数*/
		String source = "A";//A-代理商发起、M-商户发起
		String version = "V1";//版本号
		String cipherType = "AES";//加密类型
		String partnerId = SyfConstant.partnerId;//代理商id
		String accessToken = new Date().getTime()+"";//每次访问的唯一标示
		String signType = "MD5";//RSA或MD5
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("partnerId", partnerId);
		reqMap.put("accessToken", accessToken);
		//获取密文字段
		String outerMerchId = saruLruid;//代理商端商户号
		String merchEmail = "799853040@qq.com";//商户邮箱
		String merchName = merchantInfo.getMerName();//商户名称
		String corpName = merchantInfo.getMerName();//公司名称
		String merchClassify = "10";//商户类型，10个人，20个体工商，30企业
		String merchAddress = "北京丰台区";//商户地址
		String receiveSmsPhone = merchantInfo.getPhone();//接受通知手机号
		String merchantCityCode = "1000";//城市编码，写死北京市
		String merchantProvinceCode = "110";//省编码，写死北京市
		String businessCode = "210";//微信行业类目编码
		String businessCodeZFB = "2015050700000010";//支付宝行业类目编码
		String cmNotifyUrl = SyfConstant.merRegisteNotifyUrl;//商户审核通过后会向该地址发送通知，详情请参照商户入驻回调接口
		String idNumber = merchantInfo.getIdCard();//身份证号
		String acctNo = cardNo;//银行卡号
		String accountType = "2";//结算类型，1：对公，2：对私
		String contactorNm = merchantInfo.getMerName();//结算人姓名
		String contactorCell = reservedPhone;//结算人手机
		String contactorMail = "799853040@qq.com";//联系人邮箱
		String acctBankname = merchantInfo.getBankName();//银行名称
		String bankNo = merchantInfo.getBankNo();//支行号
		String t0drawFee = SyfConstant.sxf;//t0提现手续费
		String t0tradeRate = merchantDao.selectWKT0feeRate(saruLruid.substring(saruLruid.length() - 4));//t0交易费率
		String t1drawFee = SyfConstant.sxf;//t1提现手续费
		String t1tradeRate = t0tradeRate;//t1交易费率
		String balanceType = "SMZF_ZHHQ_HD_T0";//结算类型，SMZF_ZHHQ_HD_T0、SMZF_ZHHQ_HD_T1
		
		String corpOwnerNm = merchantInfo.getMerName();//法人姓名
		String ownerCertId = merchantInfo.getIdCard();//法人身份证号

		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("outerMerchId", outerMerchId);
		reqBusiMap.put("merchEmail", merchEmail);
		reqBusiMap.put("merchName", merchName);
		reqBusiMap.put("corpName", corpName);
		reqBusiMap.put("merchClassify", merchClassify);
		reqBusiMap.put("merchAddress", merchAddress);
		reqBusiMap.put("corporationId", "532501100006302");
		reqBusiMap.put("receiveSmsPhone", receiveSmsPhone);
		reqBusiMap.put("merchantCityCode", merchantCityCode);
		reqBusiMap.put("merchantProvinceCode", merchantProvinceCode);
		reqBusiMap.put("businessCode", businessCode);
		reqBusiMap.put("businessCodeZFB", businessCodeZFB);
		reqBusiMap.put("cmNotifyUrl", cmNotifyUrl);
		reqBusiMap.put("idNumber", idNumber);
		reqBusiMap.put("acctNo", acctNo);
		reqBusiMap.put("accountType", accountType);
		reqBusiMap.put("contactorNm", contactorNm);
		reqBusiMap.put("contactorCell", contactorCell);
		reqBusiMap.put("contactorMail", contactorMail);
		reqBusiMap.put("acctBankname", acctBankname);
		reqBusiMap.put("bankNo", bankNo);
		reqBusiMap.put("t0drawFee", t0drawFee);
		reqBusiMap.put("t0tradeRate", t0tradeRate);
		reqBusiMap.put("t1drawFee", t1drawFee);
		reqBusiMap.put("t1tradeRate", t1tradeRate);
		reqBusiMap.put("balanceType", balanceType);
		reqBusiMap.put("corpOwnerNm", corpOwnerNm);
		reqBusiMap.put("ownerCertId", ownerCertId);
		
		String md5Key = SyfConstant.md5Key;
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = SyfConstant.merRegiste_url;
		
		/*HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		try {
			Commons.validate_decode(httpMsg, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("返回结果："+httpMsg.getResBusiMsg());
		System.out.println("验签结果："+httpMsg.isVerify());
		Map<String, String> resultMap = new HashMap<String, String>();
		JSONObject returnMap = JSONObject.parseObject(httpMsg.getResBusiMsg());
		String resCode = (String) returnMap.get("resCode");
		String retMsg = httpMsg.getResBusiMsg();*/
	}
}
