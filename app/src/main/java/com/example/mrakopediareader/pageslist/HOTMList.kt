package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.api.dto.Page
import io.reactivex.rxjava3.core.Observable

class HOTMList : PagesList() {
    override fun getPages(): Observable<List<Page>> {
        return api.getHOTM()
    }
}