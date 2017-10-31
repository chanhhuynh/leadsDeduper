import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LeadsDeduper {
    private String inputFile;
    private String outputFile = "output.json";
    private BufferedReader leadsFile;

    // leads will hold initial and final values
    private List<Lead> leads;
    private List<Lead> duplicateLeads = new ArrayList<>();

    private void getArguments(String[] arguments){
        OptionParser optionParser = new OptionParser();
        String[] inputFileArgs = { "f", "file" };
        optionParser.acceptsAll(Arrays.asList(inputFileArgs), "Input file path")
                .withRequiredArg()
                .required()
                .ofType(String.class);
        String[] outputFileArgs = { "o", "output" };
        optionParser.acceptsAll(Arrays.asList(outputFileArgs), "Output file path")
                .withOptionalArg()
                .ofType(String.class);

        OptionSet options = optionParser.parse(arguments);

        inputFile = (String)options.valueOf("file");
        if (options.valueOf("output") != null){
            outputFile = (String)options.valueOf("output");
        }
    }

    private void readJsonLeads() throws IOException {
        Gson gson = new Gson();

        try {
            leadsFile = new BufferedReader(new FileReader(inputFile));
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonObject jobject = gson.fromJson(leadsFile, JsonObject.class);
        Type listType =  new TypeToken<List<Lead>>(){}.getType();
        // leads key only
        leads = gson.fromJson(jobject.getAsJsonArray("leads"), listType);

        System.out.println("Source record: " + leads.size() + " records");
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
        System.out.println("Finding duplicate records and updating old fields...");

        ListIterator leadIterator = leads.listIterator();

        // iterator to make the lambdas a little easier with .skip(index) for the check
        while (leadIterator.hasNext()){
            Lead lead = (Lead)leadIterator.next();
            Boolean duplicateId, duplicateEmail;

            // check if there are duplicate _id or email properties
            duplicateId = leads.stream()
                    .skip(leadIterator.nextIndex())
                    .anyMatch(_lead -> {
                        if (_lead._id.equals(lead._id)){
                            diffLeads(lead, _lead);
                            return true;
                        } else return false;
                    });
            duplicateEmail = leads.stream()
                    .skip(leadIterator.nextIndex())
                    .anyMatch(_lead -> {
                        if (_lead.email.equals(lead.email)){
                            diffLeads(lead, _lead);
                            return true;
                        } else return false;
                    });
            // add if either _id or email are duplicates
            if (duplicateId || duplicateEmail){
                duplicateLeads.add(lead);
            }
        }
    }

    // mostly for logging purposes
    private void diffLeads(Lead oldLead, Lead newLead){
        // get all attributes in the Lead object in case more fields are added
        Class lead = null;
        try{

            lead = Class.forName("Lead");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        // reflect to get all associated Lead fields
        assert lead != null;
        Field[] fields = lead.getDeclaredFields();

        for (Field field : fields){
            field.setAccessible(true);
            try{
                Object oldValue = field.get(oldLead);
                Object newValue = field.get(newLead);

                if (!oldValue.equals(newValue)){
                    System.out.println("Updating " + field.getName() + " : " + oldValue + " -> " + newValue);
                } else{
                    System.out.println("Duplicate " + field.getName() + " : " + oldValue);
                }
            } catch(IllegalAccessException e){
                e.printStackTrace();
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

        System.out.println("Output record:");
        System.out.print(gson.toJson(jsonObject));

        try (Writer writer = new FileWriter(outputFile)){
            gson.toJson(jsonObject, writer);
        } catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        LeadsDeduper leadsDeduper = new LeadsDeduper();

        try{
            leadsDeduper.getArguments(args);
            leadsDeduper.readJsonLeads();
            leadsDeduper.findDuplicates();
            leadsDeduper.deduplicateLeads();
            leadsDeduper.saveLeadsList();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
