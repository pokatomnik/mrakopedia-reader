package com.example.mrakopediareader;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public class Contact {
    private final String issuesUrl;

    private final String telegramUrl;

    private final String mailUrl;

    public Contact(Resources resources) {
        issuesUrl = resources.getString(R.string.report_issue);
        telegramUrl = resources.getString(R.string.open_telegram);
        mailUrl = resources.getString(R.string.open_email);
    }

    public Intent newIssue() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(issuesUrl));
    }

    public Intent openTelegram() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl));
    }

    public Intent openMailClient() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(mailUrl));
    }
}
