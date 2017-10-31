import com.google.gson.annotations.SerializedName;

class Lead {
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
}
