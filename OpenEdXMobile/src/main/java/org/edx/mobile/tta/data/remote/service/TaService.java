package org.edx.mobile.tta.data.remote.service;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.data.model.BookmarkResponse;
import org.edx.mobile.tta.data.model.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.ConfigModifiedDateResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

//TaService
public interface TaService {

    class TaProvider implements Provider<TaService> {

        @Inject
        private Retrofit retrofit;

        @Override
        public TaService get() {
            return retrofit.create(TaService.class);
        }
    }

    @GET(ApiConstants.URL_MX_GET_COLLECTION_CONFIG)
    Call<CollectionConfigResponse> getCollectionConfig();

    @GET(ApiConstants.URL_MX_GET_CONFIG_MODIFIED_DATE)
    Call<ConfigModifiedDateResponse> getConfigModifiedDate();

    @GET(ApiConstants.URL_MX_GET_COLLECTION_ITEMS)
    Call<List<CollectionItemsResponse>> getCollectionItems(
            @Query(Constants.KEY_LIST_IDS) String commaSeparatedListIds,
            @Query(Constants.KEY_SKIP) int skip,
            @Query(Constants.KEY_TAKE) int take
    );

    @GET(ApiConstants.URL_MX_GET_STATE_AGENDA_COUNT)
    Call<List<AgendaList>> getStateAgendaCount();

    @GET(ApiConstants.URL_MX_GET_MY_AGENDA_COUNT)
    Call<AgendaList> getMyAgendaCount();

    @GET(ApiConstants.URL_MX_GET_MY_AGENDA_CONTENT)
    Call<List<Content>> getMyAgendaContent();

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_SET_BOOKMARK)
    Call<BookmarkResponse> setBookmark(@FieldMap Map<String, Long> parameters);

}
