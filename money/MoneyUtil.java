package jutil;



import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author liangjianyuan
 * @version V1.0
 * @createTime 2016年9月8日 下午2:35:30
 * @description
 */
public class MoneyUtil {
	/**
	 * 中文中简写的汉字金额 经常使用
	 */
	public static String[] rmbNumbers = new String[] { "一", "二", "三", "四", "五","六", "七", "八", "九", "两", "廿", "卅", "○" };
	/**
	 * 中文中繁写的汉字金额 经常使用
	 */
	public static String[] bigNumbers = new String[] { "壹", "贰", "叁", "肆", "伍","陆", "柒", "捌", "玖", "俩", "廿", "卅", "零" };// 大写的汉字
	/**
	 * 与汉字相应的转化的数字
	 */
	public static Long[] tonumbers = new Long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L,8L, 9L, 2L, 2L, 3L, 0L };// 转化为阿拉伯数字
	/**
	 * 倍数关键词 简写 注意：一定要由大到小
	 */
	public static String[] rmbMult = new String[] { "億", "萬", "仟", "佰", "拾", "元" , "角", "分"};
	/**
	 * 与倍数关键词对应的倍数
	 */
	
    private static final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";  
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";  
    private static final double MAX_VALUE = 9999999999999.99D;  
    
	public static Double[] toMult = new Double[] { 100000000D, 10000D, 1000D, 100D,10D,1D,0.1,0.01};// 转化为阿拉伯的倍数
	//保留后面两位小数
	public static DecimalFormat dcf = new DecimalFormat("##0.00");
	
	public static String formatBigMoney(String money) {
		String reg = "([壹|贰|叁|肆|伍|陆|柒|捌|玖|拾]{1,}[分|角|元|拾|佰|仟|萬|億]{0,}零?){1,}";
		
		money=getComplex(money);								   //替换简体
		Matcher matcher = Pattern.compile(reg).matcher(money);     //匹配中文金额
		String bigMoney = "";
		while (matcher.find()) {
			bigMoney = matcher.group();
			if(Objects.equals("", bigMoney)){
				continue;
			}
			money = money.replace(bigMoney, rmbBigToSmall(bigMoney));
		}
		return money;
	}
	public static String getComplex(String money){
		if(!Objects.equals("", money)){
			money=money.replace("一", "壹").replace("二", "贰")
					.replace("三", "叁").replace("四", "肆")
					.replace("五", "伍").replace("六", "陆")
					.replace("七", "柒").replace("八", "捌")				//把简体换成繁体中文
					.replace("九", "玖").replace("十", "拾")
					.replace("百", "佰").replace("千", "仟")
					.replace("万", "萬").replace("亿", "億")
					.replaceAll("(圓|圆)", "元");
					;
			return money;
		}
		return "";
	} 
	public static String rmbBigToSmall(String money) {
		Double number = 0D;
		// 遍历倍数的中文词遍历的时候一定要注意 选取的倍数词为最后一个倍数词,此次遍历为第一次遍历
		for (int i = 0; i < rmbMult.length; i++) {
			int index = money.lastIndexOf(rmbMult[i]);
			if (index >= 0) {
				String storeMult = money.substring(0, index);
				money = money.substring(index + 1);
				/** 对于 十九万 这样的特殊的十的情况进行特殊处理 */
				if ((storeMult == null || storeMult.length() <= 0)
						&& toMult[i].intValue() == 10.0) {
					number = number + toMult[i];
				} else {
					number = number + (toMult[i] * getPrexNum(storeMult));
				}
			}
		}
		/**
		 * 个位数的处理
		 */
		number = number + getNumByBig(money);
		return dcf.format(number);
	}
	private static Double getPrexNum(String storeMult) {
		Double result = 0D;
		for (int i = 0; i < rmbMult.length; i++) {
			int index = storeMult.lastIndexOf(rmbMult[i]);
			if (index >= 0) {
				String storeMult2 = storeMult.substring(0, index);
				storeMult = storeMult.substring(index + 1);
				if (Objects.equals("", storeMult2)&& toMult[i].intValue() == 10) {
					result = result + toMult[i];
				} else {
					result += getNumByBig(storeMult2) * toMult[i];
				}
			}
		}
		if (storeMult != null && storeMult.length() > 0) {
			result = result + getNumByBig(storeMult);
		}
		return result;
	}
	private static Long getNumByBig(String big) {
		Long result = 0L;
		for (int j = 0; j < rmbNumbers.length; j++) {
			big = big.replace(rmbNumbers[j], tonumbers[j].toString());
			big = big.replace(bigNumbers[j], tonumbers[j].toString());
		}
		try {
			result = Long.valueOf(big);
		} catch (Exception e) {
			result = 0L;
		}
		return result;
	}
	public static String formatMoney(String money) {
		money = formatBigMoney(money);
		String endString=""; 
		Matcher matcher = null;
		// 用于保存格式化前的字符串
		String oldStr;
		// 用于保存格式化后的字符串
		String newStr;
		// 壹、贰、叁、肆、伍、陆、柒、捌、玖、拾、佰、仟
		money = money.replace(",", "").replace(":", "：")
					 .replace(")", "）").replace("(", "（")
					 .replaceAll("\\s", "")
					 ;
		
		
		String reg0 = "\\d+(\\.\\d*)?(佰|仟|拾|萬|億|元)?";
		matcher = Pattern.compile(reg0).matcher(money);
		while (matcher.find()) {
			oldStr = matcher.group().trim();
			Double tmp =0.0;
			if(oldStr.contains("拾")){
				tmp=Double.parseDouble(oldStr.replaceAll("拾", "").trim());
				tmp *= 10.00;
			}
			else if(oldStr.contains("佰")){
				tmp=Double.parseDouble(oldStr.replaceAll("佰", "").trim());
				tmp *= 100.00;
			}
			else if(oldStr.contains("仟")){
				tmp=Double.parseDouble(oldStr.replaceAll("仟", "").trim());
				tmp *= 1000.00;
			}
			else if(oldStr.contains("萬")){
				tmp=Double.parseDouble(oldStr.replaceAll("萬", "").trim());
				tmp *= 10000.00;
			}
			else if(oldStr.contains("億")){
				tmp=Double.parseDouble(oldStr.replaceAll("億", "").trim());
				tmp *= 100000000.00;
			}
			else {
				tmp=Double.parseDouble(oldStr.replaceAll("元", "").trim());
			}
			newStr = dcf.format(tmp);
			money = money.replace(oldStr, newStr);
			if(money.matches("\\d+(\\.\\d+)?(佰|仟|拾|萬|億|元).*")&&money.matches(".*?[\u4e00-\u9fa5].*")){
				money=formatMoney(money);
			}
		}
		return money.replace("元", "").trim()+endString;
	}
	//方法是整数部分每一位数都是有着固定单位的
	public static String NumtoStr(String money){
		if(Objects.equals("", money)){
			return "";
		}
		System.out.println("输入:"+money);
		String number; //整数部分
		String ratiol=""; //小数部分
		if(money.contains(".")){
			number=money.substring(0, money.indexOf("."));
			ratiol=money.substring(money.indexOf(".")+1);
		}else{
			number=money;
		}
		//最高处理到千亿
		if(number.length()>=13){
			return "金额过大";
		}
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i <number.length() ; i++) {
			
			String str=String.valueOf(number.charAt(number.length()-1-i));
			int j=i+1;
			if(j==1){
				sb.append("元"+str);
			}
			else if(j==2){
				sb.append("十"+str);
			}
			else if(j==3){
				sb.append("百"+str);
			}
			else if(j==4){
				sb.append("千"+str);
			}
			else if(j==5){
				sb.append("万"+str);
			}
			else if(j==6){
				sb.append("十"+str);
			}
			else if(j==7){
				sb.append("百"+str);
			}
			else if(j==8){
				sb.append("千"+str);
			}
			else if(j==9){
				sb.append("亿"+str);
			}
			else if(j==10){
				sb.append("十"+str);
			}
			else if(j==11){
				sb.append("百"+str);
			}
			else if(j==12){
				sb.append("千"+str);
			}
		}
		number=sb.reverse().toString();
		number=number.replaceAll("0[千百十]{1,}", "0").replaceAll("0+", "0").replace("0万", "万")
				.replace("0亿", "亿").replace("0元", "元")
				;
		System.out.println(number);
		number=number
		.replace("1", "壹").replace("2", "贰")
		.replace("3", "叁").replace("4", "肆")
		.replace("5", "伍").replace("6", "陆")
		.replace("7", "柒").replace("8", "捌")				//把简体换成繁体
		.replace("9", "玖").replace("十", "拾")
		.replace("百", "佰").replace("千", "仟")
		.replace("万", "萬").replace("亿", "億")
		.replace("元", "圓").replace("0","零");
		
		
		//小数部分 最小到分
		if(!Objects.equals("", ratiol)){
			String s1=String.valueOf(ratiol.charAt(0))+"角";
			
			if(ratiol.length()>1){
				s1=s1+String.valueOf(ratiol.charAt(1))+"分";
			}
			if(ratiol.indexOf("0")!=ratiol.lastIndexOf("0")){
				s1="";
			}
			ratiol=s1
			.replaceAll("0角", "0").replaceAll("0分", "")
			.replace("1", "壹").replace("2", "贰")
			.replace("3", "叁").replace("4", "肆")
			.replace("5", "伍").replace("6", "陆")
			.replace("7", "柒").replace("8", "捌")				//把简体换成繁体
			.replace("9", "玖").replace("0","零");
			
		}
		return number+ratiol;
	}
	 public static String change(double v) {  
         if (v < 0 || v > MAX_VALUE){  
             return "参数非法!";  
         }  
         long l = Math.round(v * 100);  
         if (l == 0){  
             return "零元整";  
         }  
         String strValue = l + "";  
         // i用来控制数  
         int i = 0;  
         // j用来控制单位  
         int j = UNIT.length() - strValue.length();  
         String rs = "";  
         boolean isZero = false;  
         for (; i < strValue.length(); i++, j++) {  
          char ch = strValue.charAt(i);  
          if (ch == '0') {  
           isZero = true;  
           if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {  
            rs = rs + UNIT.charAt(j);  
            isZero = false;  
           }  
          } else {  
           if (isZero) {  
            rs = rs + "零";  
            isZero = false;  
           }  
           rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);  
          }  
         }  
         if (!rs.endsWith("分")) {  
          rs = rs + "整";  
         }  
         rs = rs.replaceAll("亿万", "亿");  
         return rs;  
        }  

}
