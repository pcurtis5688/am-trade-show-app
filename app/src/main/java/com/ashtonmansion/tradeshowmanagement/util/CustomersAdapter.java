package com.ashtonmansion.tradeshowmanagement.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
public class CustomersAdapter extends BaseAdapter implements Filterable {
    private List<Customer> originalCustomerList;
    private List<Customer> filteredCustomerList;
    private Filter customerFilter;
    private LayoutInflater layoutInflater;

    public CustomersAdapter(Context context, List<Customer> customers) {
        this.originalCustomerList = customers;
        this.filteredCustomerList = customers;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public Customer getItem(int position) {
        return filteredCustomerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return filteredCustomerList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_customer, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.tvCustomerName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String phoneNumberIfExists;
        String emailAddressIfExists;
        if (null != filteredCustomerList.get(position).getPhoneNumbers() && filteredCustomerList.get(position).getPhoneNumbers().size() > 0) {
            phoneNumberIfExists = "Phone Number: " + filteredCustomerList.get(position).getPhoneNumbers().get(0).getPhoneNumber();
        } else {
            phoneNumberIfExists = "Phone Number: N/A";
        }
        if (null != filteredCustomerList.get(position).getEmailAddresses() && filteredCustomerList.get(position).getEmailAddresses().size() > 0) {
            emailAddressIfExists = "Email Address: " + filteredCustomerList.get(position).getEmailAddresses().get(0).getEmailAddress();
        } else {
            emailAddressIfExists = "Email Address: N/A";
        }
        String existingCustomerTextviewString =
                parent.getContext().getResources().getString(R.string.existing_customer_textview_string,
                        filteredCustomerList.get(position).getLastName(),
                        filteredCustomerList.get(position).getFirstName());
        holder.textView.setText(existingCustomerTextviewString);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (customerFilter == null) {
            customerFilter = new CustomersFilter();
        }
        return customerFilter;
    }

    private class ViewHolder {
        TextView textView;
    }

    private class CustomersFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<Customer> list = originalCustomerList;
            int count = list.size();
            final ArrayList<Customer> nList = new ArrayList<>(count);

            String filterableString;
            for (Customer checkCust : list) {
                filterableString = checkCust.getLastName() + "," + checkCust.getFirstName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nList.add(checkCust);
                }
            }
            results.values = nList;
            results.count = nList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredCustomerList = (ArrayList<Customer>) results.values;
            notifyDataSetChanged();
        }
    }
}
