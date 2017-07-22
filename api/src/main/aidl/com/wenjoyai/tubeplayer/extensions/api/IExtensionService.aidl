package com.wenjoyai.tubeplayer.extensions.api;

import com.wenjoyai.tubeplayer.extensions.api.IExtensionHost;
import com.wenjoyai.tubeplayer.extensions.api.VLCExtensionItem;

interface IExtensionService {
    // Protocol version 1
    oneway void onInitialize(int index, in IExtensionHost host);
    oneway void browse(int intId, String stringId); // longId?
    oneway void refresh();
}
