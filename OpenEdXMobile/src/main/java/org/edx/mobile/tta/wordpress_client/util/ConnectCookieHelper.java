package org.edx.mobile.tta.wordpress_client.util;

import org.edx.mobile.tta.data.remote.api.MxCookiesAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.edx.mobile.util.BrowserUtil.loginAPI;

public class ConnectCookieHelper {

    public boolean isCookieExpire()
    {
        boolean isExpire=true;
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");

            String old= loginAPI.getConnectCookiesTimeStamp();
            String current = simpleDateFormat.format(new Date());

            //old= "20-01-2017-09-43-12";
            // current = "30-02-2017-09-43-12";

            if(old!=null&& !old.equals(""))
            {
                Date cookieStoreDate = simpleDateFormat.parse(old);

                Date cuerrentDate = simpleDateFormat.parse(current);

                long diff = cuerrentDate.getTime()-cookieStoreDate.getTime();
                // System.out.println ("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)<10)
                {
                    isExpire= false;
                }
            }

        }catch (ParseException ex){
            ex.printStackTrace();
        }
        return isExpire;
    }

    public void refreshCookie()
    {
        new MxCookiesAPI().execute();
    }

}
