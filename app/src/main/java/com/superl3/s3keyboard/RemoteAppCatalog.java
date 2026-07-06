package com.superl3.s3keyboard;

final class RemoteAppCatalog {
    private static final Entry[] ENTRIES = {
            new Entry("tv.parsec.client", "parsec"),
            new Entry("com.limelight", "moonlight"),
            new Entry("com.microsoft.rdc.android", "microsoft_rdp"),
            new Entry("com.microsoft.rdc.androidx", "microsoft_rdp"),
            new Entry("com.google.chromeremotedesktop", "chrome_remote_desktop"),
            new Entry("com.valvesoftware.steamlink", "steam_link"),
            new Entry("com.anydesk.anydeskandroid", "anydesk"),
            new Entry("com.teamviewer.teamviewer.market.mobile", "teamviewer"),
            new Entry("com.teamviewer.quicksupport.market", "teamviewer")
    };

    private RemoteAppCatalog() {
    }

    static String defaultAutoPackageList() {
        StringBuilder builder = new StringBuilder();
        for (Entry entry : ENTRIES) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(entry.packageName);
        }
        return builder.toString();
    }

    static String[] reportFamilies() {
        java.util.LinkedHashSet<String> families = new java.util.LinkedHashSet<>();
        for (Entry entry : ENTRIES) {
            families.add(entry.family);
        }
        return families.toArray(new String[0]);
    }

    static String familyForPackage(String packageName) {
        String normalized = AppPackageCatalog.normalizePackageName(packageName);
        for (Entry entry : ENTRIES) {
            if (entry.packageName.equals(normalized)) {
                return entry.family;
            }
        }
        return null;
    }

    static String reportFamilyForPackage(String packageName) {
        String family = familyForPackage(packageName);
        if (family != null) {
            return family;
        }
        return AppPackageCatalog.normalizePackageName(packageName).isEmpty() ? "unknown" : "custom";
    }

    static String remoteProfileIdForPackage(String packageName) {
        String family = familyForPackage(packageName);
        return family == null ? "remote_desktop" : "remote_" + family;
    }

    private static final class Entry {
        final String packageName;
        final String family;

        Entry(String packageName, String family) {
            this.packageName = packageName;
            this.family = family;
        }
    }
}
