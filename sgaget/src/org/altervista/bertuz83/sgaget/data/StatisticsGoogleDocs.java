package org.altervista.bertuz83.sgaget.data;

import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bertuz
 * Project: sgaget
 * Creatuion date: 24/06/13, 01:39
 */
public class StatisticsGoogleDocs implements TrackRecordStatisticsDAO {
    private String emailToSend= "";


    public StatisticsGoogleDocs(String emailToSend){
        this.emailToSend= emailToSend;
    }

    @Override
    public void sendStatisticItem(TrackRecord sendingRecord) throws IOException {
        HttpClient client = new DefaultHttpClient();
        UrlEncodedFormEntity form;

        HttpPost post = new HttpPost("https://docs.google.com/forms/d/1Exbs4dlF9WB1xkv6Uf7aG4KqUK0d_nUI0EHR5BXRiTU/formResponse");
        List<BasicNameValuePair> results= new ArrayList<BasicNameValuePair>();

        results.add(new BasicNameValuePair("entry.1286232480", emailToSend));
        results.add(new BasicNameValuePair("entry.1492112641", "" + sendingRecord.getQualityRate()));
        results.add(new BasicNameValuePair("entry.1173768986", "" + sendingRecord.getTrafficRate()));
        results.add(new BasicNameValuePair("entry.928184534", "" + sendingRecord.getNotes()));
        results.add(new BasicNameValuePair("entry.1364666326", sendingRecord.getFinishHotpoint()));
        results.add(new BasicNameValuePair("entry.2121547039", sendingRecord.getFinishhour()));
        results.add(new BasicNameValuePair("entry.1668121993", sendingRecord.getStartHotpoint()));
        results.add(new BasicNameValuePair("entry.1531889462", sendingRecord.getStarthour()));
        results.add(new BasicNameValuePair("entry.668451419",  sendingRecord.getDay()));
        if(sendingRecord.getTransportationType().startsWith("__other_option__")){
            results.add(new BasicNameValuePair("entry.1890014093", "__other_option__"));
            results.add(new BasicNameValuePair("entry.1890014093.other_option_response", sendingRecord.getTransportationType().substring(16)));
        }
        else
            results.add(new BasicNameValuePair("entry.1890014093", sendingRecord.getTransportationType()));

        form = new UrlEncodedFormEntity(results, HTTP.UTF_8);
        post.setEntity(form);
        client.execute(post);

    }
}
