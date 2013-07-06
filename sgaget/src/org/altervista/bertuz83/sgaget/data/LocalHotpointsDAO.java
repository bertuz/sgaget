package org.altervista.bertuz83.sgaget.data;

import org.altervista.bertuz83.sgaget.business.Hotpoint;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 */
public interface LocalHotpointsDAO {
    public void open() throws SQLException;
    public void close();

    public void openSecure() throws SQLException;
    public void closeSecure();

    public boolean insertHotpoint(Hotpoint hotpoint);
    public void eraseHotpoints();
    public ArrayList<Hotpoint> getHotpointsList();
    public int getNrHotpoints();
}
