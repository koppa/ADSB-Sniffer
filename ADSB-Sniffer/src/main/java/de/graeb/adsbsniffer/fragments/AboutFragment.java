package de.graeb.adsbsniffer.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.graeb.adsbsniffer.ui.LicensesDialog;

/**
 * @author markus
 */
public class AboutFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static AboutFragment newInstance(int sectionNumber) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AboutFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        final ImageView discoLogo = (ImageView) rootView.findViewById(R.id.image_disco_logo);
        final TextView discoPage = (TextView) rootView.findViewById(R.id.text_disco_page);
        final TextView email1 = (TextView) rootView.findViewById(R.id.text_email1);
        final TextView email2 = (TextView) rootView.findViewById(R.id.text_email2);
        final Button buttonLicenses = (Button) rootView.findViewById(R.id.button_licenses);

        final OnClickUri disco = new OnClickUri("https://disco.informatik.uni-kl.de/");
        discoLogo.setOnClickListener(disco);
        discoPage.setOnClickListener(disco);
        email1.setOnClickListener(new OnClickUri("mailto: " + email1.getText()));
        email2.setOnClickListener(new OnClickUri("mailto: " + email2.getText()));

        buttonLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LicensesDialog licensesDialog = new LicensesDialog();
                licensesDialog.show(getFragmentManager(), null);
            }
        });

        return rootView;
    }

    /**
     * Opens an uri
     */
    private class OnClickUri implements View.OnClickListener {
        private final Uri uri;

        public OnClickUri(CharSequence uri) {
            this.uri = Uri.parse(uri.toString());
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
