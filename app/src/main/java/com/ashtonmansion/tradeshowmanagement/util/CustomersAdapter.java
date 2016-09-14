package com.ashtonmansion.tradeshowmanagement.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.v3.customers.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 9/13/2016.
 */
public class CustomersAdapter extends ArrayAdapter<Customer> implements Filterable {
    private List<Customer> originalCustomerList;
    private List<Customer> customerList;
    private Filter customerFilter;

    public CustomersAdapter(Context context, List<Customer> customers) {
        super(context, 0, customers);
        this.originalCustomerList = customers;
        this.customerList = customers;
    }

    @Override
    public Customer getItem(int position) {
        return originalCustomerList.get(position);
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

    @Override
    public Filter getFilter() {
        if (customerFilter == null){
            customerFilter = new CustomersFilter();
        }
        return customerFilter;
    }

    private class CustomersFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                //nothing to constrain, return whole list
                results.values = originalCustomerList;
                results.count = originalCustomerList.size();
            } else {
                List<Customer> filteredCustomerList = new ArrayList<>();
                for (Customer customer : customerList) {
                    if (customer.getLastName().toUpperCase().startsWith(constraint.toString())) {
                        filteredCustomerList.add(customer);
                    }
                }

                results.values = filteredCustomerList;
                results.count = filteredCustomerList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                customerList = (List<Customer>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}
