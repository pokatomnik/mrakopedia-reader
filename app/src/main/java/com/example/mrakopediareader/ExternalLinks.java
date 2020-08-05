package com.example.mrakopediareader;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public class ExternalLinks {
    private final String issuesUrl;

    private final String telegramUrl;

    private final String mailUrl;

    public ExternalLinks(Resources resources) {
        issuesUrl = resources.getString(R.string.drawer_report_issue);
        telegramUrl = resources.getString(R.string.drawer_open_telegram);
        mailUrl = resources.getString(R.string.drawer_open_email);
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

    public Intent openWebsiteUrl(Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }
}
