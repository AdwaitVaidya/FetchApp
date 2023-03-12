package com.example.feedapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {
    private ApiService apiService = ApiServiceFactory.create();

    private MutableLiveData<List<Item>> items = new MutableLiveData<>();
    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void fetchItems() {
        apiService.getItems().enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful()) {
                    List<Item> itemsList = response.body();
                    List<Item> filteredItemsList = new ArrayList<>();
                    for (Item item : itemsList) {
                        if (item.getName() != null && !item.getName().isEmpty()) {
                            filteredItemsList.add(item);
                        }
                    }
                    Map<Integer, List<Item>> groupedItemsMap = groupItemsByListId(filteredItemsList);
                    List<Item> finalItemsList = flattenGroupedItemsMap(groupedItemsMap);
                    items.setValue(finalItemsList);
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                items.setValue(null);
            }
        });
    }

    private Map<Integer, List<Item>> groupItemsByListId(List<Item> items) {
        Map<Integer, List<Item>> groupedItemsMap = new TreeMap<>();
        for (Item item : items) {
            int listId = item.getListId();
            List<Item> group = groupedItemsMap.get(listId);
            if (group == null) {
                group = new ArrayList<>();
                groupedItemsMap.put(listId, group);
            }
            group.add(item);
        }
        return groupedItemsMap;
    }

    private List<Item> flattenGroupedItemsMap(Map<Integer, List<Item>> groupedItemsMap) {
        List<Item> flattenedList = new ArrayList<>();
        for (Map.Entry<Integer, List<Item>> entry : groupedItemsMap.entrySet()) {
            List<Item> group = entry.getValue();
            Collections.sort(group, new Comparator<Item>() {
                @Override
                public int compare(Item o1, Item o2) {
                    if (o1.getListId() == o2.getListId()) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        return Integer.compare(o1.getListId(), o2.getListId());
                    }
                }
            });
            flattenedList.addAll(group);
        }
        return flattenedList;
    }
}
