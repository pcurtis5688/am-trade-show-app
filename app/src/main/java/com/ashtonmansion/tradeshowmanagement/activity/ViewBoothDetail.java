package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v3.customers.Address;
import com.clover.sdk.v3.customers.Customer;
import com.clover.sdk.v3.customers.EmailAddress;
import com.clover.sdk.v3.customers.PhoneNumber;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class ViewBoothDetail extends AppCompatActivity {
    private Context viewBoothDetailContext;
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private Item booth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBoothDetailContext = getBaseContext();
        Bundle extras = getIntent().getExtras();
        booth = (Item) extras.get("booth");

        //UI FIELDS
        setContentView(R.layout.activity_view_booth_detail);
        TextView boothDetailHeaderTV = (TextView) findViewById(R.id.booth_detail_header);
        boothDetailHeaderTV.setText(booth.getName());


    }

    protected void finalizeReservation(View view) {
        String reservationConfirmationToast = getResources().getString(R.string.finalized_reservation_confirmation);
        Toast.makeText(viewBoothDetailContext, reservationConfirmationToast, Toast.LENGTH_SHORT).show();
        pd = new ProgressDialog(viewBoothDetailContext);
        pd.setMessage("Finalizing Reservation....");
        //GRAB FIELD HANDLERS
        EditText customerFirstNameField = (EditText) findViewById(R.id.br_customer_first_name);
        EditText customerLastNameField = (EditText) findViewById(R.id.br_customer_last_name);
        EditText customerAddress1Field = (EditText) findViewById(R.id.customer_address_1_field);
        EditText customerAddress2Field = (EditText) findViewById(R.id.customer_address_2_field);
        EditText customerAddress3Field = (EditText) findViewById(R.id.customer_address_3_field);
        EditText customerAddressCityField = (EditText) findViewById(R.id.customer_address_city_field);
        Spinner customerAddressStateSpinner = (Spinner) findViewById(R.id.customer_address_state_spinner);
        EditText customerAddressZipField = (EditText) findViewById(R.id.customer_address_zip_field);
        EditText customerEmailField = (EditText) findViewById(R.id.customer_email_field);
        EditText customerPhoneField = (EditText) findViewById(R.id.customer_phone_field);

        //FETCH CUSTOMER DATA FROM FIELDS
        Customer reservingCustomer = new Customer();
        reservingCustomer.setFirstName(customerFirstNameField.getText().toString());
        reservingCustomer.setLastName(customerLastNameField.getText().toString());

        Address customerAddressObj = new Address();
        customerAddressObj.setAddress1(customerAddress1Field.getText().toString());
        customerAddressObj.setAddress2(customerAddress2Field.getText().toString());
        customerAddressObj.setAddress3(customerAddress3Field.getText().toString());
        customerAddressObj.setCity(customerAddressCityField.getText().toString());
        customerAddressObj.setState(customerAddressStateSpinner.getSelectedItem().toString());
        customerAddressObj.setZip(customerAddressZipField.getText().toString());
        List<Address> customerAddressInList = new ArrayList<>();
        customerAddressInList.add(customerAddressObj);
        reservingCustomer.setAddresses(customerAddressInList);

        List<EmailAddress> customerEmailInList = new ArrayList<>();
        List<PhoneNumber> customerPhoneInList = new ArrayList<>();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setEmailAddress(customerEmailField.getText().toString());
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneNumber(customerPhoneField.getText().toString());
        customerEmailInList.add(emailAddress);
        customerPhoneInList.add(phoneNumber);

        reservingCustomer.setEmailAddresses(customerEmailInList);
        reservingCustomer.setPhoneNumbers(customerPhoneInList);


        ////ACCESS CLOVER
        merchantAccount = CloverAccount.getAccount(viewBoothDetailContext);
        inventoryConnector = new InventoryConnector(viewBoothDetailContext, merchantAccount, null);

        //DETERMINE IF NEED TO ADD NEW CUSTOMER, IF NOT, ASSOCIATE THE TWO
        //AND UPDATE THE INVENTORY


    }

    private class CreateCustomerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //// TODO: 8/26/2016
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    private class FinalizeReservationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}
