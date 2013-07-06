package org.altervista.bertuz83.sgaget.data;

import org.altervista.bertuz83.sgaget.business.TrackRecord;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * User: bertuz
 * Project: sgaget
 */
public interface TrackRecordStatisticsDAO {

    public void sendStatisticItem(TrackRecord sendingRecord) throws IOException;
}
