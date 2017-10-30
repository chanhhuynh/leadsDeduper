import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Lead {
    @SerializedName("_id")
    String _id;
    @SerializedName("email")
    String email;
    @SerializedName("firstName")
    String firstName;
    @SerializedName("lastName")
    String lastName;
    @SerializedName("address")
    String address;
    @SerializedName("entryDate")
    String entryDate;

//    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
//    Date _entryDate = new Date();
//    {
//        try {
//            _entryDate = df.parse(entryDate);
//        } catch (java.text.ParseException e){
//            e.printStackTrace();
//        }
//    }
}
