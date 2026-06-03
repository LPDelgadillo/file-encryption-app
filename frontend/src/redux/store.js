import { configureStore } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query/react'
import { encryptionApiBack } from './peticiones/encryptionApi';


export const store = configureStore({
    reducer: {
        // user: consultUserSliceReducer,
        // counter: counterReducer,
        // signatures: signaturesReducer,
        [encryptionApiBack.reducerPath]: encryptionApiBack.reducer,
        // [lambdaFirmaApi.reducerPath]: lambdaFirmaApi.reducer,
    },

    middleware: (getDefaultMiddleware) =>

        getDefaultMiddleware().concat(
            encryptionApiBack.middleware,
            // lambdaFirmayaApi.middleware
        ),
})


setupListeners(store.dispatch)