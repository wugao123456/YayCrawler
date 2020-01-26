package yaycrawler.spider.geetest;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaycrawler.common.utils.HttpUtil;

import java.util.Map;

/**
 * Created by  yuananyun on 2017/3/25.
 */
public class GeetestValidationProvider {

    private static Logger logger = LoggerFactory.getLogger(GeetestValidationProvider.class);
    private static String GEETEST_CRACKER_URL_TEMPLATE = "http://120.77.36.82:8080/rest/geetest/cracker?targetUrl=%s&userId=%s";

    /**
     * 获取极验验证码破解参数
     *
     * @param targetUrl 包含极验验证码的目标网站
     * @param userId    打码平台的用户id
     * @return
     */
    public static GeetestValidation getValidation(String targetUrl, String userId) {
        try {
            Map<String, Object> response = HttpUtil.getInstance().doGetForMap(String.format(GEETEST_CRACKER_URL_TEMPLATE, targetUrl, userId), null, null);
            if (response != null) {
                GeetestValidation validation = new GeetestValidation();
                boolean isSuccess = MapUtils.getBooleanValue(response, "success");
                if (isSuccess) {
                    Map geetestData = MapUtils.getMap(response, "data");
                    validation.setGeetest_challenge(MapUtils.getString(geetestData, "geetest_challenge"));
                    validation.setGeetest_seccode(MapUtils.getString(geetestData, "geetest_seccode"));
                    validation.setGeetest_validate(MapUtils.getString(geetestData, "geetest_validate"));
                } else {
                    validation.setCode(MapUtils.getString(response, "errorCode"));
                    validation.setMessage(MapUtils.getString(response, "message"));
                }
                logger.debug("[getValidation] {}", validation.toString());
                return validation;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

}
