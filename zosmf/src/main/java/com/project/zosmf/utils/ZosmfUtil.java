package com.project.zosmf.utils;
import com.project.zosmf.entity.JobInfo;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

/**
 * 与 z/OSMF 交换信息
 */
public class ZosmfUtil {

    private static HttpComponentsClientHttpRequestFactory requestFactory;

    //禁用证书验证
    static {
        CloseableHttpClient httpClient = SslUtil.SslHttpClientBuild();
        requestFactory
                = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
    }

    //向主机发起请求
    public static <T> T go(HttpSession session, String path, HttpMethod method, Object body, HttpHeaders headers, Class<T> responseType) {
        Object ZOSMF_JSESSIONID = session.getAttribute("ZOSMF_JSESSIONID");
        Object ZOSMF_LtpaToken2 = session.getAttribute("ZOSMF_LtpaToken2");
        Object ZOSMF_Address = session.getAttribute("ZOSMF_Address");

        String urlOverHttps = "https://" + ZOSMF_Address.toString() + path;

        // set header with jsessionid and token
        if (headers == null) {
            // headers fallback
            headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
        }
        headers.add("Cookie", ZOSMF_JSESSIONID.toString() + ";" + ZOSMF_LtpaToken2);
        HttpEntity<?> request = new HttpEntity<>(body, headers);

        return new RestTemplate(requestFactory).exchange(urlOverHttps, method, request, responseType).getBody();
    }

    //获取作业状态
    public static boolean isReady(HttpSession session, String path, int seconds) {
        Object ZOSMF_JSESSIONID = session.getAttribute("ZOSMF_JSESSIONID");
        Object ZOSMF_LtpaToken2 = session.getAttribute("ZOSMF_LtpaToken2");
        Object ZOSMF_Address = session.getAttribute("ZOSMF_Address");

        String urlOverHttps = "https://" + ZOSMF_Address.toString() + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add("Cookie", ZOSMF_JSESSIONID.toString() + ";" + ZOSMF_LtpaToken2);
        HttpEntity<?> request = new HttpEntity<>(null, headers);

        for (int i = 0; i < seconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ResponseEntity<JobInfo> response = new RestTemplate(requestFactory).exchange(urlOverHttps, HttpMethod.GET, request, JobInfo.class);
            if (response.getBody() != null && response.getBody().getStatus().equals("OUTPUT")) {
                return true;
            }
        }
        return false;
    }
}
