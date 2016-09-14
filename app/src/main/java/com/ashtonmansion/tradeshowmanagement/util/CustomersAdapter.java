package com.ashtonmansion.tradeshowmanagement.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.v1.customer.Customer;

import java.util.ArrayList;

/**
 * Created by paul on 9/13/2016.
 */
public class CustomersAdapter extends ArrayAdapter<Customer> {
    private ArrayList<Customer> customers;

    public CustomersAdapter(Context context, ArrayList<Customer> customers) {
        super(context, 0, customers);
        this.customers = customers;
    }

    @Override
    public Customer getItem(int position) {
        return customers.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Customer customer = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_customer, parent, false);
        }
        TextView tvCustomerName = (TextView) convertView.findViewById(R.id.tvCustomerName);
        TextView tvCustomerPhone = (TextView) convertView.findViewById(R.id.tvCustomerPhone);
        tvCustomerName.setText(customer.getLastName() + ", " + customer.getFirstName());
        tvCustomerPhone.setText(customer.getPhoneNumbers().toString());
        return convertView;
    }
}
