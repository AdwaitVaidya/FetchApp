package com.example.feedapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ItemAdapter
        itemAdapter = new ItemAdapter();
        recyclerView.setAdapter(itemAdapter);

        // Load data from URL
        new LoadDataTask().execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List<Item>> {

        @Override
        protected List<Item> doInBackground(Void... voids) {
            try {
                // Download data from URL
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Parse JSON data
                Gson gson = new Gson();
                Type type = new TypeToken<List<Item>>() {}.getType();
                List<Item> itemList = gson.fromJson(reader, type);

                // Close resources
                reader.close();
                inputStream.close();
                connection.disconnect();

                // Filter out items with blank or null name
                return itemList.stream()
                        .filter(item -> item.getName() != null && !item.getName().isEmpty())
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Item> itemList) {
            super.onPostExecute(itemList);

            if (itemList != null) {
                // Group items by listId
                Map<Integer, List<Item>> itemMap = itemList.stream()
                        .collect(Collectors.groupingBy(Item::getListId));

                // Sort items by listId and name
                List<Item> sortedItemList = itemMap.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream())
                        .sorted(Comparator.comparing(Item::getListId).thenComparing(Item::getName))
                        .collect(Collectors.toList());

                // Update ItemAdapter with sorted list
                itemAdapter.setItems(sortedItemList);
            } else {
                // Handle error
                Toast.makeText(MainActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
