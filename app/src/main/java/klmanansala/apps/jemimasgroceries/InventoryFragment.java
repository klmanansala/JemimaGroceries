package klmanansala.apps.jemimasgroceries;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class InventoryFragment extends Fragment{

    public InventoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        String dummyData[] = new String[]{
                "sample item",
                "sample item",
                "sample item",
                "sample item",
                "sample item",
                "sample item",
                "sample item"
        };

        ArrayAdapter dummyAdapter = new ArrayAdapter<String>(getActivity(), R.layout.inventory_item_layout, dummyData);

        ListView groceryList = (ListView) view.findViewById(R.id.listview_inventory);
        groceryList.setAdapter(dummyAdapter);

        ImageButton addButton = (ImageButton) view.findViewById(R.id.btn_add_inventory);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addInventoryIntent = new Intent(getActivity(), AddInventoryItemActivity.class);
                startActivity(addInventoryIntent);
            }
        });

        return view;
    }
}
