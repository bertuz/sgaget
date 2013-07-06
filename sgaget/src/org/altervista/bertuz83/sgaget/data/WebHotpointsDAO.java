package org.altervista.bertuz83.sgaget.data;

import org.altervista.bertuz83.sgaget.business.Hotpoint;

import java.io.IOException;

/**
 * User: bertuz
 * Project: sgaget
 * Creation date: 26/05/13, 16:21
 */
public interface WebHotpointsDAO{
    public int getVersion() throws IOException;
    public int getNrHotpoints() throws IOException;
    public Hotpoint getNextHotpoint() throws IOException;
}
