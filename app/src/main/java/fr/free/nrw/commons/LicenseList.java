package fr.free.nrw.commons;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LicenseList {
    private Map<String, License> licenses = new HashMap<>();
    private Resources res;

    public LicenseList(Activity activity) {
        res = activity.getResources();
        XmlPullParser parser = res.getXml(R.xml.wikimedia_licenses);
        String namespace = "https://www.mediawiki.org/wiki/Extension:UploadWizard/xmlns/licenses";
        while (xmlFastForward(parser, namespace, "license")) {
            String id = parser.getAttributeValue(null, "id");
            String template = parser.getAttributeValue(null, "template");
            String url = parser.getAttributeValue(null, "url");
            String name = nameForTemplate(template);
            License license = new License(id, template, url, name);
            licenses.put(id, license);
        }
    }

    public Collection<License> values() {
        return licenses.values();
    }

    public License get(String key) {
        return licenses.get(key);
    }

    @Nullable
    License licenseForTemplate(String template) {
        String ucTemplate = new PageTitle(template).getDisplayText();
        for (License license : values()) {
            if (ucTemplate.equals(new PageTitle(license.getTemplate()).getDisplayText())) {
                return license;
            }
        }
        return null;
    }

    private String nameIdForTemplate(String template) {
        // hack :D (converts dashes and periods to underscores)
        // cc-by-sa-3.0 -> cc_by_sa_3_0
        return "license_name_" + template.toLowerCase(Locale.ENGLISH).replace("-",
                "_").replace(".", "_");
    }

    private String nameForTemplate(String template) {
        int nameId = res.getIdentifier("fr.free.nrw.commons:string/"
                + nameIdForTemplate(template), null, null);
        return (nameId != 0) ? res.getString(nameId) : template;
    }

    /**
     * Fast-forward an XmlPullParser to the next instance of the given element
     * in the input stream (namespaced).
     *
     * @param parser
     * @param namespace
     * @param element
     * @return true on match, false on failure
     */
    private boolean xmlFastForward(XmlPullParser parser, String namespace, String element) {
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG &&
                        parser.getNamespace().equals(namespace) &&
                        parser.getName().equals(element)) {
                    // We found it!
                    return true;
                }
            }
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}