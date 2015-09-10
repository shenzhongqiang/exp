package main.java.product;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Enumeration;
import main.java.exceptions.InvalidCurrency;

public class CurrencyTable {
    private static HashMap<String, CurrencyPair> currencyTable = getCurrencyTable();

    private static HashMap getCurrencyTable() {
        InputStream input = null;
        Properties prop = null;
        HashMap result = new HashMap<String, CurrencyPair>();
        try {
            input = new FileInputStream("src/main/resources/currency.properties");
            prop = new Properties();
            prop.load(input);
            Enumeration e = prop.propertyNames();
            while(e.hasMoreElements()) {
                String pair = (String) e.nextElement();
                String value = (String) prop.getProperty(pair);
                String[] parts = value.split(" ");
                double valuePerPoint = Double.parseDouble(parts[0]);
                double marginPerMiniLot = Double.parseDouble(parts[1]);
                double point = Double.parseDouble(parts[2]);
                CurrencyPair cp = new CurrencyPair(pair, point, marginPerMiniLot, valuePerPoint);
                result.put(pair, cp);
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if(input != null) {
                try {
                    input.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static double getPoint(String pair) {
        if(!currencyTable.containsKey(pair)) {
            throw new InvalidCurrency(pair);
        }
        return currencyTable.get(pair).getPoint();
    }

    public static double getMarginPerMiniLot(String pair) {
        if(!currencyTable.containsKey(pair)) {
            throw new InvalidCurrency(pair);
        }
        return currencyTable.get(pair).getMarginPerMiniLot();
    }

    public static double getValuePerPoint(String pair) {
        if(!currencyTable.containsKey(pair)) {
            throw new InvalidCurrency(pair);
        }
        return currencyTable.get(pair).getValuePerPoint();
    }
}
