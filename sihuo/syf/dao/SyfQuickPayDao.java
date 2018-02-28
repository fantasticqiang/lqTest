package com.syf.dao;

import cn.phoneposp.dao.ConnectionSource;
import cn.phoneposp.entity.CreditCardList;
import cn.phoneposp.entity.Merchant;

import com.syf.model.MerReportModel;
import com.syf.model.MerchantEnterModel;
import com.yufutong.model.TransactionDetailModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyfQuickPayDao {

	

	/**
	 * 插入商户报户
	 */
	public boolean insertMerchantAllMeaasge(String saruLruid, String merName,
			String merId, String phone, String merStatus, String idCard,
			String drawName, String cardNo, String bankName,
			String reservedPhone, String bankNo, String bankAccountType,
			String merPro, String regionId, String feeRate, String feeRate0,
			String retCode, String retMsg,String business) {

		boolean flag = true;

		String sql = "insert into MERCHANT_REPORT"
				+ "(SARULRU_ID,MER_NAME,MER_ID,PHONE,MER_STATUS,ID_CARD,"
				+ "DRAW_NAME,CARD_NO,BANK_NAME,RESERVED_PHONE,BANK_NO,"
				+ "BANK_ACCOUNT_TYPE,MER_PRO,REGION_ID,FEERATE,FEERATE0,"
				+ "RET_CODE,RET_MSG,BUSINESS)"
				+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			con.setAutoCommit(false);
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			state.setString(2, merName);
			state.setString(3, merId);
			state.setString(4, phone);
			state.setString(5, merStatus);
			state.setString(6, idCard);
			state.setString(7, drawName);
			state.setString(8, cardNo);
			state.setString(9, bankName);
			state.setString(10, reservedPhone);
			state.setString(11, bankNo);
			state.setString(12, bankAccountType);
			state.setString(13, merPro);
			state.setString(14, regionId);
			state.setString(15, feeRate);
			state.setString(16, feeRate0);
			state.setString(17, retCode);
			state.setString(18, retMsg);
			state.setString(19, business);

			state.executeUpdate();
			state.close();

			con.commit();

		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return flag;
	}

	public String selectAreaCode(String subCard) {
		String sql = "select FY_REGION_CD FROM creditcard_area_code where FRONTSIX_IDCARD=?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null;
		String areaCode = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, subCard);
			res = state.executeQuery();
			if (res.next()) {
				areaCode = res.getString(1);
				return areaCode;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, res, state);
		}
		return areaCode;
	}

	// 根据商户号查询拿到一个实体
	public MerchantEnterModel queryMerchantBysaruId(String saruLruid) {
		String sql = "select SARU_CHIEF,SARU_CERTNO,SARU_PHONE,SARU_BACKNAME,SARU_BANKLINKED ,SARU_BACKCARD,r_b_saleru.CITY_ID,city.PROVINCE_ID from r_b_saleru LEFT JOIN city  on r_b_saleru.CITY_ID = city.CITY_ID where saru_lruid=?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		MerchantEnterModel consumer = new MerchantEnterModel();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			set = state.executeQuery();
			if (set.next()) {
				consumer.setMerName(set.getString(1));
				consumer.setIdCard(set.getString(2));
				consumer.setPhone(set.getString(3));
				consumer.setBankName(set.getString(4));
				consumer.setBankNo(set.getString(5));
				consumer.setCardNo(set.getString(6));
				consumer.setCityId(set.getString(7));
				consumer.setProvinceId(set.getString(8));
				return consumer;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return consumer;
	}

	// 根据商户号查询商户的无卡T1-D0费率
	public String selectWKT0feeRate(String saruLruid) {
		String sql = "select SCALC_FREECARDVALUE from r_p_scalecommission a WHERE a.SARU_ID=?";
		String freecardValue = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			set = state.executeQuery();
			if (set.next()) {
				freecardValue = set.getString(1);
				return freecardValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return freecardValue;
	}

	/**
	 * 插入商户报户
	 * 
	 */

	public boolean insertMerchantEnter(String saruLruid, String merName,
			String merId, String phone, String idCard, String drawName,
			String cardNo, String bankName, String reservedPhone,
			String bankNo, String bankAccountType, String merPro,
			String regionId, String feeRate, String feeRate0, String retCode,
			String retMsg, String merStatus) {

		boolean flag = true;

		String sql = "insert into MERCHANT_REPORT"
				+ "(SARULRU_ID,MER_NAME,MER_ID,PHONE,ID_CARD,"
				+ "DRAW_NAME,CARD_NO,BANK_NAME,RESERVED_PHONE,BANK_NO,"
				+ "BANK_ACCOUNT_TYPE,MER_PRO,REGION_ID,FEERATE,FEERATE0,"
				+ "RET_CODE,RET_MSG,MER_STATUS)"
				+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			con.setAutoCommit(false);
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			state.setString(2, merName);
			state.setString(3, merId);
			state.setString(4, phone);
			state.setString(5, idCard);
			state.setString(6, drawName);
			state.setString(7, cardNo);
			state.setString(8, bankName);
			state.setString(9, reservedPhone);
			state.setString(10, bankNo);
			state.setString(11, bankAccountType);
			state.setString(12, merPro);
			state.setString(13, regionId);
			state.setString(14, feeRate);
			state.setString(15, feeRate0);
			state.setString(16, retCode);
			state.setString(17, retMsg);
			state.setString(18, merStatus);

			state.executeUpdate();
			state.close();

			con.commit();

		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return flag;
	}

	// 判断MERCHANT_REPORT-商户报户表中是否存在手机号对应的记录
	public boolean queryMerIsExist(String phone) {
		String sql = "select * from MERCHANT_REPORT  where phone= ?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, phone);
			set = state.executeQuery();
			if (set.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return false;
	}

	// 根据手机号查询商户开通报户的状态
	public String selectMerStatus(String phone) {
		String sql = "select MER_STATUS from MERCHANT_REPORT  WHERE PHONE=?";
		String merStatus = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, phone);
			set = state.executeQuery();
			if (set.next()) {
				merStatus = set.getString(1);
				return merStatus;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return merStatus;
	}

	// 更新商户开户状态
	public boolean updateMerStatus(String phone, String merStatus,String retMsg) {
		String sql = "UPDATE MERCHANT_REPORT SET MER_STATUS = ?,RET_MSG = ? WHERE PHONE = ?";
		Connection con = null;
		PreparedStatement state = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, merStatus);
			state.setString(2, retMsg);
			state.setString(3, phone);
			state.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, null, state);
		}
		return false;
	}

	public String selectSaruLruidByOrderId(String orderId) {
		String sql = "select SARULRUID from WATER  WHERE ORDERID=?";
		String saruLruid = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, orderId);
			set = state.executeQuery();
			if (set.next()) {
				saruLruid = set.getString(1);
				return saruLruid;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return saruLruid;
	}

	public String selectMerIdBySaruLruid(String saruLruid) {
		String sql = "select MER_ID from MERCHANT_REPORT  WHERE SARULRU_ID=?";
		String merId = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			set = state.executeQuery();
			if (set.next()) {
				merId = set.getString(1);
				return merId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return merId;
	}
	/**
	 * 以下为新增方法
	 * 查询
	 */
	public String selectMerIdAndIdCardBySaruLruid(String saruLruid) {
		String sql = "select MER_ID,ID_CARD from MERCHANT_REPORT  WHERE SARULRU_ID=?";
		String merId = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			set = state.executeQuery();
			if (set.next()) {
				merId = set.getString(1);
				merId += ","+set.getString(2);
				return merId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return merId;
	}
	
	// 根据交易订单号订单号,从无卡water表中查询卡号
	public String selectCardNoByOrderId(String orderId) {
		String sql = "select CARDNO from WATER  WHERE ORDERID=?";
		String cardNo = "";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, orderId);
			set = state.executeQuery();
			if (set.next()) {
				cardNo = set.getString(1);
				return cardNo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return cardNo;
	}
	
	// (御付通通道)无卡交易详情查询
	public TransactionDetailModel getTransactionDetailByOrderId(String orderId) {
		String sql = "SELECT rw.BCCON_MONEY,rw.BCCON_SALEMONEY,tsn.BANK_TRAN_ID,tsn.BCCON_ORDERNUM,tsn.CASH_STATUS,tsn.CREATE_TIME "
				+ "FROM r_w_bankcardconsumer rw LEFT JOIN t0_cashinfo_new tsn ON rw.BCCON_ORDERNUM=tsn.BCCON_ORDERNUM "
				+ "WHERE tsn.BCCON_ORDERNUM=?";
		TransactionDetailModel detailModel = null;
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, orderId);
			set = state.executeQuery();
			if (set.next()) {
				detailModel = new TransactionDetailModel();
				detailModel.setOrderAmt(set.getDouble(1));
				detailModel.setSettleAmt(set.getDouble(2));
				detailModel.setPlatSeq(set.getString(3));
				detailModel.setOrderId(set.getString(4));
				detailModel.setOrderStatus(set.getString(5));
				detailModel.setOrderTime(set.getString(6).substring(0,
						set.getString(6).length() - 2));
				return detailModel;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return detailModel;

	}

	public boolean updateCardMain(String cardNo, String bankName, String bankNo,String reservedPhone,String saruLruid) {
		  String sql = "UPDATE R_B_SALERU  SET SARU_BACKCARD = ?,SARU_BACKNAME= ?,SARU_BANKLINKED=?,SARU_PHONE=?" +
		  		"WHERE SARU_LRUID = ?";
	        Connection con = null;
	        PreparedStatement state = null;
	        try {
	            con = ConnectionSource.getConnection();
	            state = con.prepareStatement(sql);
	            state.setString(1, cardNo);
	            state.setString(2, bankName);
	            state.setString(3, bankNo);
	            state.setString(4, reservedPhone);
	            state.setString(5, saruLruid);
	            state.executeUpdate();
	            return true;
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            ConnectionSource.close(con, null, state);
	        }
		return true;
	}

	public boolean updateRetMsg(String phone, String retMsg) {
		String sql = "UPDATE MERCHANT_REPORT SET RET_MSG = ? WHERE PHONE = ?";
		Connection con = null;
		PreparedStatement state = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, retMsg);
			state.setString(2, phone);
			state.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, null, state);
		}
		return false;
	}

	public List<TransactionDetailModel> selectTransactionDetailModel() {
		  String sql = "select ORDERID,AMOUNT,TRADETIME,STATUS from water where STATUS='1'";
	        List<TransactionDetailModel> transactionDetailModel = new ArrayList<TransactionDetailModel>();
	        Connection con = null;
	        PreparedStatement state = null;
	        ResultSet set = null;
	        try {
	            con = ConnectionSource.getConnection();
	            state = con.prepareStatement(sql);
	            set = state.executeQuery();
	            while (set.next()) {
	            	TransactionDetailModel consumer = new TransactionDetailModel();
	                consumer.setOrderId(set.getString(1));
	                consumer.setOrderAmt(set.getDouble(2));
	                consumer.setOrderStatus(set.getString(3));
	                transactionDetailModel.add(consumer);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            ConnectionSource.close(con, set, state);
	        }
	        return transactionDetailModel;
	}

	public boolean updateStatus(String orderId, String status) {
		String sql = "UPDATE water SET STATUS = ? WHERE ORDERID = ?";
		Connection con = null;
		PreparedStatement state = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, status);
			state.setString(2, orderId);
			state.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, null, state);
		}
		return false;
		
	}

	//查询无卡商户
	public List<Merchant> getWKT0Merchant() {
		String sql = "select MERCHANTID,MERCHANTNO,MERCHANTSECRETKEY from merchant where MERCHANTISPAY='N' and MERCHANTTYPE ='8'";
		Connection con  = null;
		PreparedStatement state =null;
		ResultSet res = null;
		List<Merchant> list = new ArrayList<Merchant>();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			res = state.executeQuery();
			while(res.next()){
				Merchant merchant = new Merchant();
				merchant.setMerchantId(res.getInt(1));
				merchant.setMerchantNo(res.getString(2));
				merchant.setMerchantSecretKey(res.getString(3));
				list.add(merchant);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConnectionSource.close(con, res, state);
		}
		return list;
	}

	//将使用无卡商户号的MERCHANTISPAY改为N
	public void updateWKT0Merchant() {
		String sql = "update merchant set MERCHANTISPAY = 'N' where MERCHANTTYPE = '2'";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null; 
		try {
			con  = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConnectionSource.close(con, res, state);
		}
		
		
	}

	//判断无卡订单号是否存在
	public boolean tradeIdIsNow(String orderId) {
		String sql = "select * from water where ORDERID = ?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		boolean b = true;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, orderId);
			set = state.executeQuery();
			if (set.next()) {
				b = false;
				return b;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return b;
	}


	 public Integer insertFreeCardWithPlace(String orderid, String saruLruid, Double amount, String lastno,
                                           String cardno, String lng_lat, String place,String notifyUrl) {
		String sql = "insert into WATER(ORDERID,AMOUNT,LASTNO,"
				+ "SARULRUID,STATUS,CARDNO,TRADETIME,CARDTYPE,LNG_LAT,PLACE_NAME,SALENAME)"
				+ "values(?,?,?,?,?,?,?,?,?,?,?) ";
        Integer verifyId = getMaxId("WATER", "ID");
        Connection con = null;
        PreparedStatement state = null;
        try {
            con = ConnectionSource.getConnection();
            con.setAutoCommit(false);
            state = con.prepareStatement(sql);
            state.setString(1, orderid);
            state.setDouble(2, amount);
            state.setString(3, lastno);
            state.setString(4, saruLruid);
            int aa = 0;
            state.setInt(5, aa);
            state.setString(6, cardno);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            state.setString(7, sdf.format(new Date()));
            state.setString(8, "信用卡");
            state.setString(9, lng_lat);
            state.setString(10, place);
            state.setString(11, notifyUrl);
            state.executeUpdate();
            con.commit();
            return verifyId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionSource.close(con, null, state);
        }
        return null;
    }

	 	
	public static Integer getMaxId(String tableName, String columnName) {
	        String sql = "select ifnull(max(" + columnName + "),0)+1  from " + tableName;
	        Connection con = null;
	        PreparedStatement state = null;
	        ResultSet set = null;
	        try {
	            con = ConnectionSource.getConnection();
	            state = con.prepareStatement(sql);
	            set = state.executeQuery();
	            if (set.next()) {
	                if (set.getInt(1) == 0) {
	                    return 1;
	                } else {
	                    return set.getInt(1);
	                }

	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            ConnectionSource.close(con, set, state);
	        }
	        return null;
	    }

	public String getNotifyUrl(String orderId) {
		String sql = "select SALENAME from water where ORDERID = ?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1,orderId);
			set = state.executeQuery();
			if (set.next()) {
				String notifyUrl = set.getString(1);
				return notifyUrl;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConnectionSource.close(con, set, state);
		}
		
		return "";
	}

	public CreditCardList getCreditCard(String saruLruid, String id) {
		String sql = "select SARU_LRUID,PHONE,CVV2,CARD_NUM,EXP_DATE,CREATE_DATE,SARU_BANKLINKED,SARU_ACCOUNTNAME,ID,SARU_BANKNAME from CREDIT_CARD_LIST where SARU_LRUID = ?  and ID=?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet set = null;
		CreditCardList creditCard = null;
		try {
			con = ConnectionSource.getConnection();
			con.setAutoCommit(false);
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			state.setString(2, id);
			set = state.executeQuery();
			con.commit();
			while (set.next()) {
				creditCard = new CreditCardList();
				creditCard.setSaruLruid(set.getString(1));
				creditCard.setPhone(set.getString(2));
				creditCard.setCvv2(set.getString(3));
				creditCard.setCardNum(set.getString(4));
				creditCard.setExpDate(set.getString(5));
				creditCard.setCreateDate(set.getString(6));
				creditCard.setSaruBankLinked(set.getString(7));
				creditCard.setSaruAccountName(set.getString(8));
				creditCard.setId(set.getInt(9));
				creditCard.setBankName(set.getString(10));
			}
			return creditCard;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, set, state);
		}
		return null;
	}
	
	/**
	 * 从商户报户表中，查询商户的merId和key
	 */
	public MerReportModel getMerReport(String SARULRU_ID){
		String sql = "select MER_ID,BUSINESS from MERCHANT_REPORT where SARULRU_ID = ?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null;
		MerReportModel merReportModel = null;
		merReportModel = new MerReportModel();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, SARULRU_ID);
			res = state.executeQuery();
			while (res.next()) {
				String merId = res.getString(1);
				String key = res.getString(2);
				merReportModel.setKey(key);
				merReportModel.setMerId(merId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, res, state);
		}
		return merReportModel;
		
	}
	
	/**
	 * 根据上游给的merchId查询这个商户的key
	 */
	public MerReportModel getKeyBySyfMerchId(String merchId){
		String sql = "select MER_ID,BUSINESS from MERCHANT_REPORT where MER_ID = ?";
		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null;
		MerReportModel merReportModel = null;
		merReportModel = new MerReportModel();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, merchId);
			res = state.executeQuery();
			while (res.next()) {
				String merId = res.getString(1);
				String key = res.getString(2);
				merReportModel.setKey(key);
				merReportModel.setMerId(merId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, res, state);
		}
		return merReportModel;
	}

}
