
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class OrderCompos {

    private static final String rscPath = "/src/main/resources/";
    private static final String inputFile = "input.txt";
    private static final String outputFile = "output.txt";

    public static void main(String[] args) {

        Path currentRelativePath = Paths.get("");
        String inF = currentRelativePath.toAbsolutePath() + rscPath + inputFile;
        String outF = currentRelativePath.toAbsolutePath() + rscPath + outputFile;

        JSONParser parser = new JSONParser();
        JSONArray newOrders = new JSONArray();

        try (Reader reader = new FileReader(inF)) {
            newOrders = (JSONArray) parser.parse(reader);
            //System.out.println(newOrders);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashMap a = processingNewOrders(newOrders);
        JSONArray b = finalProcessing (a);
        System.out.println("end");

        try (FileWriter file = new FileWriter(outF)) {
            file.write(b.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    static private HashMap processingNewOrders (JSONArray orders){
        HashMap <Long,HashMap<Long,JSONObject>> processedOrders = new HashMap<Long,HashMap<Long,JSONObject>>();
        Iterator<JSONObject> iter = orders.iterator();
        while (iter.hasNext()){
            JSONObject nowOrder = iter.next();
            Long order = (Long) nowOrder.get("order_id");
            Long item = (Long) nowOrder.get("item_id");
            Long event = (Long) nowOrder.get("event_id");

            if (!processedOrders.containsKey(order)){
                HashMap <Long, JSONObject> a = new HashMap<>();
                a.put(item,nowOrder);
                processedOrders.put(order, a);
            }
            else {
                HashMap<Long, JSONObject> a = processedOrders.get(order);
                if (!a.containsKey(item)) a.put(item, nowOrder);
                else{
                    if ((Long)a.get(item).get("event_id") < event){
                        if (nowOrder.get("status").equals("CANCEL")) a.remove(item);
                        else if (((Long)nowOrder.get("count")-(Long)nowOrder.get("return_count")) < 1 ) a.remove(item);
                        else a.replace(item,nowOrder);
                    }
                }
            }
        }
        return processedOrders;
    }
    static private JSONArray finalProcessing (HashMap<Long,HashMap<Long,JSONObject>> orders){
        //массив orders
        JSONArray finish = new JSONArray();
        for (Map.Entry<Long, HashMap<Long,JSONObject>> set : orders.entrySet()){
            HashMap<Long, JSONObject> a = set.getValue();
            // массив items
            JSONArray b = finalItemsInOrder(a);
            if (!b.isEmpty()){
                JSONObject e = new JSONObject();
                e.put("id", set.getKey());
                e.put("items", b);
                finish.add(e);
            }

        }
        return finish;
    }

    static private JSONArray finalItemsInOrder (HashMap<Long,JSONObject> order){
        JSONArray b = new JSONArray();
        for (Map.Entry<Long, JSONObject> belowSet : order.entrySet()){
            JSONObject c = belowSet.getValue();
            Long amount = (Long)c.get("count")-(Long)c.get("return_count");
            //if ((c.get("status")).equals("OK") && ( amount > 0)){
                JSONObject d = new JSONObject();
                d.put("count", amount);
                d.put("id", c.get("item_id"));
                b.add(d);
            //}
        }
        return b;
    }
}

/*class Order  {
    private Long event_id;
    private Long order_id;
    private Long item_id;
    private Long count;
    private Long return_count;
    private String status;
*/