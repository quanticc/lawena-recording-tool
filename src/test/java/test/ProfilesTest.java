
package test;

import static org.junit.Assert.*;

import com.github.iabarca.lwrt.tf2.TF2Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

public class ProfilesTest {

    @Test
    public void test() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("output: " + gson.toJson(new TF2Profile()));
    }

}
