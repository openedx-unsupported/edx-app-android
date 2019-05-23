package org.edx.mobile.tta.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.module.registration.model.RegistrationOption;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    private static final String statesJson = "[{\n" +
            "        \"name\": \"Andhra Pradesh\",\n" +
            "        \"value\": \"AP\"\n" +
            "      }, {\n" +
            "        \"name\": \"Arunachal Pradesh\",\n" +
            "        \"value\": \"AR\"\n" +
            "      }, {\n" +
            "        \"name\": \"Assam\",\n" +
            "        \"value\": \"AS\"\n" +
            "      }, {\n" +
            "        \"name\": \"Bihar\",\n" +
            "        \"value\": \"BR\"\n" +
            "      }, {\n" +
            "        \"name\": \"Chhattisgarh\",\n" +
            "        \"value\": \"CG\"\n" +
            "      }, {\n" +
            "        \"name\": \"Chandigarh\",\n" +
            "        \"value\": \"CHGH\"\n" +
            "      }, {\n" +
            "        \"name\": \"Dadra and Nagar Haveli\",\n" +
            "        \"value\": \"DN\"\n" +
            "      }, {\n" +
            "        \"name\": \"Daman and Diu\",\n" +
            "        \"value\": \"DD\"\n" +
            "      }, {\n" +
            "        \"name\": \"Delhi\",\n" +
            "        \"value\": \"DL\"\n" +
            "      }, {\n" +
            "        \"name\": \"Goa\",\n" +
            "        \"value\": \"GA\"\n" +
            "      }, {\n" +
            "        \"name\": \"Gujarat\",\n" +
            "        \"value\": \"GJ\"\n" +
            "      }, {\n" +
            "        \"name\": \"Haryana\",\n" +
            "        \"value\": \"HR\"\n" +
            "      }, {\n" +
            "        \"name\": \"Himachal Pradesh\",\n" +
            "        \"value\": \"HP\"\n" +
            "      }, {\n" +
            "        \"name\": \"Jammu and Kashmir\",\n" +
            "        \"value\": \"JK\"\n" +
            "      }, {\n" +
            "        \"name\": \"Jharkhand\",\n" +
            "        \"value\": \"JH\"\n" +
            "      }, {\n" +
            "        \"name\": \"Karnataka\",\n" +
            "        \"value\": \"KA\"\n" +
            "      }, {\n" +
            "        \"name\": \"Kerala\",\n" +
            "        \"value\": \"KL\"\n" +
            "      }, {\n" +
            "        \"name\": \"Madhya Pradesh\",\n" +
            "        \"value\": \"MP\"\n" +
            "      }, {\n" +
            "        \"name\": \"Maharashtra\",\n" +
            "        \"value\": \"MH\"\n" +
            "      }, {\n" +
            "        \"name\": \"Manipur\",\n" +
            "        \"value\": \"MN\"\n" +
            "      }, {\n" +
            "        \"name\": \"Meghalaya\",\n" +
            "        \"value\": \"ML\"\n" +
            "      }, {\n" +
            "        \"name\": \"Mizoram\",\n" +
            "        \"value\": \"MZ\"\n" +
            "      }, {\n" +
            "        \"name\": \"Nagaland\",\n" +
            "        \"value\": \"NL\"\n" +
            "      }, {\n" +
            "        \"name\": \"Odisha\",\n" +
            "        \"value\": \"OD\"\n" +
            "      }, {\n" +
            "        \"name\": \"Punjab\",\n" +
            "        \"value\": \"PB\"\n" +
            "      }, {\n" +
            "        \"name\": \"Pondicherry\",\n" +
            "        \"value\": \"PY\"\n" +
            "      }, {\n" +
            "        \"name\": \"Rajasthan\",\n" +
            "        \"value\": \"RJ\"\n" +
            "      }, {\n" +
            "        \"name\": \"Sikkim\",\n" +
            "        \"value\": \"SK\"\n" +
            "      }, {\n" +
            "        \"name\": \"Tamil Nadu\",\n" +
            "        \"value\": \"TN\"\n" +
            "      }, {\n" +
            "        \"name\": \"Tripura\",\n" +
            "        \"value\": \"TR\"\n" +
            "      }, {\n" +
            "        \"name\": \"Uttar Pradesh\",\n" +
            "        \"value\": \"UP\"\n" +
            "      }, {\n" +
            "        \"name\": \"Uttarakhand\",\n" +
            "        \"value\": \"UK\"\n" +
            "      }, {\n" +
            "        \"name\": \"West Bengal\",\n" +
            "        \"value\": \"WB\"\n" +
            "      }]";

    private static final String districtsJson = "[\n" +
            "        {\n" +
            "          \"name\": \"Bihar\",\n" +
            "          \"value\": \"Araria,Arwal,Aurangabad(BH),Banka,Begusarai,Bhagalpur,Bhojpur,Buxar,Darbhanga,East Champaran,Gaya,Gopalganj,Jamui,Jehanabad,Kaimur (Bhabua),Katihar,Khagaria,Kishanganj,Lakhisarai,Madhepura,Madhubani,Munger,Muzaffarpur,Nalanda,Nawada,Patna,Purnia,Rohtas,Saharsa,Samastipur,Saran,Sheikhpura,Sheohar,Sitamarhi,Siwan,Supaul,Vaishali,West Champaran\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Delhi\",\n" +
            "          \"value\": \"Central Delhi,East Delhi,New Delhi,North Delhi,North East Delhi,North West Delhi,South Delhi,South West Delhi,West Delhi\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Assam\",\n" +
            "          \"value\": \"Barpeta,Bongaigaon,Cachar,Darrang,Dhemaji,Dhubri,Dibrugarh,Goalpara,Golaghat,Hailakandi,Jorhat,Kamrup,Karbi Anglong,Karimganj,Kokrajhar,Lakhimpur,Marigaon,Nagaon,Nalbari,North Cachar Hills,Sibsagar,Sonitpur,Tinsukia\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Gujarat\",\n" +
            "          \"value\": \"Ahmedabad,Amreli,Anand,Banaskantha,Bharuch,Bhavnagar,Dahod,Gandhi Nagar,Jamnagar,Junagadh,Kachchh,Kheda,Mahesana,Narmada,Navsari,Panch Mahals,Patan,Porbandar,Rajkot,Sabarkantha,Surat,Surendra Nagar,Tapi,The Dangs,Vadodara,Valsad\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Daman and Diu\",\n" +
            "          \"value\": \"Daman,Diu\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Andhra Pradesh\",\n" +
            "          \"value\": \"Adilabad,Ananthapur,Chittoor,Cuddapah,East Godavari,Guntur,Hyderabad,K.V.Rangareddy,Karim Nagar,Khammam,Krishna,Kurnool,Mahabub Nagar,Medak,Nalgonda,Nellore,Nizamabad,Prakasam,Srikakulam,Visakhapatnam,Vizianagaram,Warangal,West Godavari\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Madhya Pradesh\",\n" +
            "          \"value\": \"Alirajpur,Anuppur,Ashok Nagar,Balaghat,Barwani,Betul,Bhind,Bhopal,Burhanpur,Chhatarpur,Chhindwara,Damoh,Datia,Dewas,Dhar,Dindori,East Nimar,Guna,Gwalior,Harda,Hoshangabad,Indore,Jabalpur,Jhabua,Katni,Khandwa,Khargone,Mandla,Mandsaur,Morena,Narsinghpur,Neemuch,Panna,Raisen,Rajgarh,Ratlam,Rewa,Sagar,Satna,Sehore,Seoni,Shahdol,Shajapur,Sheopur,Shivpuri,Sidhi,Singrauli,Tikamgarh,Ujjain,Umaria,Vidisha,West Nimar\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Jammu and Kashmir\",\n" +
            "          \"value\": \"Ananthnag,Bandipur,Baramulla,Budgam,Doda,Jammu,Kargil,Kathua,Kulgam,Kupwara,Leh,Poonch,Pulwama,Rajauri,Reasi,Shopian,Srinagar,Udhampur\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Tamil Nadu\",\n" +
            "          \"value\": \"Ariyalur,Chennai,Coimbatore,Cuddalore,Dharmapuri,Dindigul,Erode,Kanchipuram,Kanyakumari,Karur,Krishnagiri,Madurai,Nagapattinam,Namakkal,Nilgiris,Perambalur,Pudukkottai,Ramanathapuram,Salem,Sivaganga,Thanjavur,Theni,Tiruchirappalli,Tirunelveli,Tiruvallur,Tiruvannamalai,Tiruvarur,Tuticorin,Vellore,Villupuram,Virudhunagar\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Dadra and Nagar Haveli\",\n" +
            "          \"value\": \"Dadra and Nagar Haveli\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Chhattisgarh\",\n" +
            "          \"value\": \"Balod,Baloda Bazar,Balrampur,Baster,Bemetara,Bijapur,Bilaspur,Dantewada,Dhamtari,Durg,Gariaband,Janjgir-Champa,Jashpur,Kanker,Kawardha,Kondagaon,Korba,Koriya,Mahasamund,Mungeli,Narayanpur,Raigarh,Raipur,Rajnandgaon,Sukma,Surajpur,Surguja\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Karnataka\",\n" +
            "          \"value\": \"Bagalkot,Bangalore,Bangalore Rural,Belgaum,Bellary,Bidar,Bijapur(KAR),Chamrajnagar,Chickmagalur,Chikkaballapur,Chitradurga,Dakshina Kannada,Davangere,Dharwad,Gadag,Gulbarga,Hassan,Haveri,Kodagu,Kolar,Koppal,Mandya,Mysore,Raichur,Ramanagar,Shimoga,Tumkur,Udupi,Uttara Kannada,Yadgir\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Mizoram\",\n" +
            "          \"value\": \"Aizawl,Champhai,Kolasib,Lawngtlai,Lunglei,Mammit,Saiha,Serchhip\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Andaman and Nicobar Islands\",\n" +
            "          \"value\": \"Nicobar,North And Middle Andaman,South Andaman\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Goa\",\n" +
            "          \"value\": \"North Goa,South Goa\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Odisha\",\n" +
            "          \"value\": \"Angul,Balangir,Baleswar,Bargarh,Bhadrak,Boudh,Cuttack,Debagarh,Dhenkanal,Gajapati,Ganjam,Jagatsinghapur,Jajapur,Jharsuguda,Kalahandi,Kandhamal,Kendrapara,Kendujhar,Khorda,Koraput,Malkangiri,Mayurbhanj,Nabarangapur,Nayagarh,Nuapada,Puri,Rayagada,Sambalpur,Sonapur,Sundergarh\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"West Bengal\",\n" +
            "          \"value\": \"Bankura,Bardhaman,Birbhum,Cooch Behar,Darjiling,East Midnapore,Hooghly,Howrah,Jalpaiguri,Kolkata,Malda,Medinipur,Murshidabad,Nadia,North 24 Parganas,North Dinajpur,Puruliya,South 24 Parganas,South Dinajpur,West Midnapore\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Maharashtra\",\n" +
            "          \"value\": \"Ahmed Nagar,Akola,Amravati,Aurangabad,Beed,Bhandara,Buldhana,Chandrapur,Dhule,Gadchiroli,Gondia,Hingoli,Jalgaon,Jalna,Kolhapur,Latur,Mumbai,Nagpur,Nanded,Nandurbar,Nashik,Osmanabad,Parbhani,Pune,Raigarh(MH),Ratnagiri,Sangli,Satara,Sindhudurg,Solapur,Thane,Wardha,Washim,Yavatmal\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Kerala\",\n" +
            "          \"value\": \"Alappuzha,Ernakulam,Idukki,Kannur,Kasargod,Kollam,Kottayam,Kozhikode,Malappuram,Palakkad,Pathanamthitta,Thiruvananthapuram,Thrissur,Wayanad\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Lakshadweep\",\n" +
            "          \"value\": \"Lakshadweep\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Chandigarh\",\n" +
            "          \"value\": \"Chandigarh\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Sikkim\",\n" +
            "          \"value\": \"East Sikkim,North Sikkim,South Sikkim,West Sikkim\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Meghalaya\",\n" +
            "          \"value\": \"East Garo Hills,East Khasi Hills,Jaintia Hills,Ri Bhoi,South Garo Hills,West Garo Hills,West Khasi Hills\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Punjab\",\n" +
            "          \"value\": \"Amritsar,Barnala,Bathinda,Faridkot,Fatehgarh Sahib,Fazilka,Firozpur,Gurdaspur,Hoshiarpur,Jalandhar,Kapurthala,Ludhiana,Mansa,Moga,Mohali,Muktsar,Nawanshahr,Pathankot,Patiala,Ropar,Rupnagar,Sangrur,Tarn Taran\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Rajasthan\",\n" +
            "          \"value\": \"Ajmer,Alwar,Banswara,Baran,Barmer,Bharatpur,Bhilwara,Bikaner,Bundi,Chittorgarh,Churu,Dausa,Dholpur,Dungarpur,Ganganagar,Hanumangarh,Jaipur,Jaisalmer,Jalor,Jhalawar,Jhujhunu,Jodhpur,Karauli,Kota,Nagaur,Pali,Rajsamand,Sawai Madhopur,Sikar,Sirohi,Tonk,Udaipur\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Tripura\",\n" +
            "          \"value\": \"Dhalai,North Tripura,South Tripura,West Tripura\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Uttar Pradesh\",\n" +
            "          \"value\": \"Agra,Aligarh,Allahabad,Ambedkar Nagar,Auraiya,Azamgarh,Bagpat,Bahraich,Ballia,Balrampur,Banda,Barabanki,Bareilly,Basti,Bijnor,Budaun,Bulandshahr,Chandauli,Chitrakoot,Deoria,Etah,Etawah,Faizabad,Farrukhabad,Fatehpur,Firozabad,Gautam Buddha Nagar,Ghaziabad,Ghazipur,Gonda,Gorakhpur,Hamirpur,Hardoi,Hathras,Jalaun,Jaunpur,Jhansi,Jyotiba Phule Nagar,Kannauj,Kanpur Dehat,Kanpur Nagar,Kaushambi,Kheri,Kushinagar,Lalitpur,Lucknow,Maharajganj,Mahoba,Mainpuri,Mathura,Mau,Meerut,Mirzapur,Moradabad,Muzaffarnagar,Pilibhit,Pratapgarh,Raebareli,Rampur,Saharanpur,Sant Kabir Nagar,Sant Ravidas Nagar,Shahjahanpur,Shrawasti,Siddharthnagar,Sitapur,Sonbhadra,Sultanpur,Unnao,Varanasi\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Jharkhand\",\n" +
            "          \"value\": \"Bokaro,Chatra,Deoghar,Dhanbad,Dumka,Garhwa,Giridih,Godda,Gumla,Hazaribag,Jamtara,Khunti,Kodarma,Latehar,Lohardaga,Pakaur,Palamu,Pashchimi Singhbhum,Purbi Singhbhum,Ramgarh,Ranchi,Sahibganj,Saraikela-kharsawan,Simdega\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Haryana\",\n" +
            "          \"value\": \"Ambala,Bhiwani,Faridabad,Fatehabad,Gurgaon,Hisar,Jhajjar,Jind,Kaithal,Karnal,Kurukshetra,Mahendragarh,Panchkula,Panipat,Rewari,Rohtak,Sirsa,Sonipat,Yamuna Nagar\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Himachal Pradesh\",\n" +
            "          \"value\": \"Bilaspur (HP),Chamba,Hamirpur(HP),Kangra,Kinnaur,Kullu,Lahul and Spiti,Mandi,Shimla,Sirmaur,Solan,Una\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Uttarakhand\",\n" +
            "          \"value\": \"Almora,Bageshwar,Chamoli,Champawat,Dehradun,Haridwar,Nainital,Pauri,Pithoragarh,Rudraprayag,Tehri,U.S. Nagar,Uttarkashi\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Arunachal Pradesh\",\n" +
            "          \"value\": \"Changlang,Dibang Valley,East Kameng,East Siang,Kurung Kumey,Lohit,Lower Dibang Valley,Lower Subansiri,Papum Pare,Tawang,Tirap,Upper Siang,Upper Subansiri,West Kameng,West Siang\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Pondicherry\",\n" +
            "          \"value\": \"Karaikal,Mahe,Pondicherry\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Manipur\",\n" +
            "          \"value\": \"Bishnupur,Chandel,Churachandpur,Imphal East,Imphal West,Senapati,Tamenglong,Thoubal,Ukhrul\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Nagaland\",\n" +
            "          \"value\": \"Dimapur,Kiphire,Kohima,Longleng,Mokokchung,Mon,Peren,Phek,Tuensang,Wokha,Zunhebotto\"\n" +
            "        }\n" +
            "      ]";

    private static final String professionJson = "[\n" +
            "        {\n" +
            "          \"name\": \"Government School Teacher\",\n" +
            "          \"value\": \"government_school_teacher\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Private School Teacher\",\n" +
            "          \"value\": \"private_school_teacher\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Principal\",\n" +
            "          \"value\": \"principal\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Teacher Trainee\",\n" +
            "          \"value\": \"teacher_trainee\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Teacher Trainer\",\n" +
            "          \"value\": \"teacher_trainer\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"CAC\",\n" +
            "          \"value\": \"cac\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"CRC\",\n" +
            "          \"value\": \"crc\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"BRC\",\n" +
            "          \"value\": \"brc\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Other\",\n" +
            "          \"value\": \"Other\"\n" +
            "        }]";

    private static final String genderJson = "[\n" +
            "        {\n" +
            "          \"name\": \"Male\",\n" +
            "          \"value\": \"m\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Female\",\n" +
            "          \"value\": \"f\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Other\",\n" +
            "          \"value\": \"o\"\n" +
            "        }\n" +
            "      ]";

    private static final String classJson = "[\n" +
            "        {\n" +
            "          \"name\": \"Primary\",\n" +
            "          \"value\": \"primary\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Middle\",\n" +
            "          \"value\": \"middle\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Secondary\",\n" +
            "          \"value\": \"secondary\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Senior Secondary\",\n" +
            "          \"value\": \"senior_secondary\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Other\",\n" +
            "          \"value\": \"Other\"\n" +
            "        }]";

    private static final String dietJson = "[\n" +
            "        {\"name\":\"Andhra Pradesh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Pondicherry\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Assam\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Bihar\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Delhi\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Gujarat\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Daman and Diu\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Dadra and Nagar Haveli\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Dadra and Nagar Haveli\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Haryana\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Himachal Pradesh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Jammu and Kashmir\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Jharkhand\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Karnataka\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Kerala\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Lakshadweep\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Madhya Pradesh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Maharashtra\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Goa\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Manipur\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Mizoram\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Nagaland\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Tripura\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Arunachal Pradesh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Meghalaya\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Odisha\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Punjab\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Chandigarh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Rajasthan\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Tamil Nadu\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Uttar Pradesh\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Uttarakhand\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"West Bengal\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Andaman and Nicobar Islands\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Sikkim\",\n" +
            "          \"value\":\"\"},\n" +
            "        {\"name\":\"Chhattisgarh\",\n" +
            "          \"value\":\"210301,210101,210303,226101,211305,211306,211102,211310,211302,211301,211101,211307,211308,211304,212101,212301,213101,214316,214307,214318,214302,214317,214309,214401,214315,214101,214102,214305,214304,214303,214320,214306,214313,223310,214312,214310,214301,214311,214314,214319,223304,223315,215304,215101,215302,215307,215303,223306,215301,215306,215305,216101,216201,218101,217101,219101,220101,221101,221302,221304,221303,211309,227101,222102,222101,222104,222103,223318,223101,223307,223314,223317,223316,223313,223301,223311,223312,223319,223305,223308,223309,224102,224101,224103,224105,224104,225101\"}\n" +
            "      ]";

    public static List<RegistrationOption> getAllStates(){
        List<RegistrationOption> states;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        states = gson.fromJson(statesJson, collectionType);
        return states;
    }

    public static List<RegistrationOption> getDistrictsByStateName(String stateName){
        List<RegistrationOption> districts = new ArrayList<>();
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        List<RegistrationOption> options = gson.fromJson(districtsJson, collectionType);
        for (RegistrationOption option: options){
            if (option.getName().equalsIgnoreCase(stateName)){
                for (String distName: option.getValue().split(",")){
                    districts.add(new RegistrationOption(distName, distName));
                }
            }
        }
        return districts;
    }

    public static List<RegistrationOption> getAllProfessions(){
        List<RegistrationOption> professions;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        professions = gson.fromJson(professionJson, collectionType);
        return professions;
    }

    public static List<RegistrationOption> getAllGenders(){
        List<RegistrationOption> genders;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        genders = gson.fromJson(genderJson, collectionType);
        return genders;
    }

    public static List<RegistrationOption> getAllClassesTaught(){
        List<RegistrationOption> classesTaught;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        classesTaught = gson.fromJson(classJson, collectionType);
        return classesTaught;
    }

    public static List<RegistrationOption> getAllDietCodesOfState(String stateName){
        List<RegistrationOption> dietCodes = new ArrayList<>();
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<RegistrationOption>>(){}.getType();
        List<RegistrationOption> options = gson.fromJson(dietJson, collectionType);
        for (RegistrationOption option: options){
            if (option.getName().equalsIgnoreCase(stateName)){
                if (!TextUtils.isEmpty(option.getValue())) {
                    for (String dietCode: option.getValue().split(",")){
                        dietCodes.add(new RegistrationOption(dietCode, dietCode));
                    }
                }
                break;
            }
        }
        return dietCodes;
    }

    public static String getStateNameFromValue(String value){
        List<RegistrationOption> states = getAllStates();
        for (RegistrationOption state: states){
            if (state.getValue().equalsIgnoreCase(value)){
                return state.getName();
            }
        }
        return value;
    }

}
