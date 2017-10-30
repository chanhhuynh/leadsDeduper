import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LeadsDeduper {
    private String filename;
    private BufferedReader leadsFile;

    // leads will hold initial and final values
    private List<Lead> leads;
    private List<Lead> duplicateLeads = new ArrayList<>();

    private void getFilename(String[] arguments){
        OptionParser optionParser = new OptionParser();
        String[] fileOptions = { "f", "file" };
        optionParser.acceptsAll(Arrays.asList(fileOptions), "Path and name of file.")
                .withRequiredArg()
                .required()
                .ofType(String.class);

        OptionSet options = optionParser.parse(arguments);

        filename = (String)options.valueOf("file");
    }

    private void readJsonLeads() throws IOException {
        Gson gson = new Gson();

        try {
            leadsFile = new BufferedReader(new FileReader(filename));
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonObject jobject = gson.fromJson(leadsFile, JsonObject.class);
        Type listType =  new TypeToken<List<Lead>>(){}.getType();
        // leads key only
        leads = gson.fromJson(jobject.getAsJsonArray("leads"), listType);
    }

    private Date stringToDate(String dateString){
        Date date = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        try{
            date = df.parse(dateString);
        }catch (java.text.ParseException e){
            e.printStackTrace();
        }

        return date;
    }

    private void findDuplicates(){
        ListIterator leadIterator = leads.listIterator();

        // iterator to make the lambdas a little easier with .skip(index) for the check
        while (leadIterator.hasNext()){
            Lead lead = (Lead)leadIterator.next();
            Boolean duplicateId, duplicateEmail;

            // check if there are duplicate _id or email properties
            duplicateId = leads.stream()
                    .skip(leadIterator.nextIndex())
                    .anyMatch(_lead -> _lead._id.equals(lead._id));
            duplicateEmail = leads.stream()
                    .skip(leadIterator.nextIndex())
                    .anyMatch(_lead -> _lead.email.equals(lead.email));

            // add if either _id or email are duplicates
            if (duplicateId || duplicateEmail){
                duplicateLeads.add(lead);
            }
        }
    }

    private void deduplicateLeads(){
        ListIterator leadIterator = leads.listIterator();

        while (leadIterator.hasNext()){
            Lead lead = (Lead)leadIterator.next();

            for (Lead duplicateLead : duplicateLeads){
                if (lead._id.equals(duplicateLead._id) || lead.email.equals(duplicateLead.email)){
                    // remove the current element if it's older than an existing dupe or if they're the same
                    // this will prefer the last entry in the list also
                    if (stringToDate(lead.entryDate).compareTo(stringToDate(duplicateLead.entryDate)) <= 0){
                        leadIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private void saveLeadsList(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("leads", gson.toJsonTree(leads));

        try (Writer writer = new FileWriter("output.json")){
            gson.toJson(jsonObject, writer);
        } catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        LeadsDeduper leadsDeduper = new LeadsDeduper();

        try{
            leadsDeduper.getFilename(args);
            leadsDeduper.readJsonLeads();
            leadsDeduper.findDuplicates();
            leadsDeduper.deduplicateLeads();
            leadsDeduper.saveLeadsList();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
