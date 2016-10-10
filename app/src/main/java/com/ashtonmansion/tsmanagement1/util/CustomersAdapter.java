package com.ashtonmansion.tsmanagement1.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ashtonmansion.tsmanagement1.R;
import com.clover.sdk.v3.customers.Customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paul on 9/13/2016.
 */
public class CustomersAdapter extends BaseAdapter implements Filterable {
    private List<Customer> originalCustomerList;
    private List<Customer> filteredCustomerList;
    private Filter customerFilter;
    private LayoutInflater layoutInflater;
    private Context adapterContext;

    public CustomersAdapter(Context context, List<Customer> customers) {
        adapterContext = context;
        Collections.sort(customers, new Comparator<Customer>() {
            public int compare(Customer customer1, Customer customer2) {
                if (customer1.getLastName() == null || customer2.getLastName() == null) {
                    return -1;
                } else {
                    return customer1.getLastName().compareTo(customer2.getLastName());
                }
            }
        });
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
        Customer customer = filteredCustomerList.get(position);

        String phoneNumberIfExists;
        String emailAddressIfExists;
        if (null != filteredCustomerList.get(position).getPhoneNumbers() && filteredCustomerList.get(position).getPhoneNumbers().size() > 0) {
            phoneNumberIfExists = " - Phone: " + filteredCustomerList.get(position).getPhoneNumbers().get(0).getPhoneNumber();
        } else {
            phoneNumberIfExists = "";
        }
        if (null != filteredCustomerList.get(position).getEmailAddresses() && filteredCustomerList.get(position).getEmailAddresses().size() > 0) {
            emailAddressIfExists = " - Email: " + filteredCustomerList.get(position).getEmailAddresses().get(0).getEmailAddress();
        } else {
            emailAddressIfExists = "";
        }
        String existingCustomerTextviewString =
                parent.getContext().getResources().getString(R.string.existing_customer_textview_string,
                        filteredCustomerList.get(position).getLastName(),
                        filteredCustomerList.get(position).getFirstName(),
                        phoneNumberIfExists,
                        emailAddressIfExists);
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

    private class fetchPhoneNosAndEmailsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}
