import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

export const encryptionApiBack = createApi({
    reducerPath: 'encryption',
    baseQuery: fetchBaseQuery({
        baseQuery: fetchBaseQuery({
            baseUrl: 'https://file-encryption-app-production.up.railway.app/api/encryption',
        }),
    }),
    endpoints: (builder) => ({

        encryptData: builder.mutation({
            query: ({ file, secretKey }) => {
                const formData = new FormData()
                formData.append('file', file)
                formData.append('secretKey', secretKey)
                return {
                    url: '/encrypt',
                    method: 'POST',
                    body: formData,
                    responseHandler: async (response) => {
                        if (!response.ok) {
                            const error = await response.text()
                            throw new Error(error)
                        }
                        const blob = await response.blob()

                        // Lee el header Content-Disposition
                        const disposition = response.headers.get('content-disposition') || ''
                        console.log('disposition:', disposition) // para debug

                        // Extrae el nombre — maneja comillas y sin comillas
                        let fileName = 'processed_file'
                        const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
                        if (match && match[1]) {
                            fileName = match[1].replace(/['"]/g, '').trim()
                        }

                        return { blob, fileName }
                    },
                }
            },
        }),

        decryptData: builder.mutation({
            query: ({ file, secretKey }) => {
                const formData = new FormData()
                formData.append('file', file)
                formData.append('secretKey', secretKey)
                return {
                    url: '/decrypt',
                    method: 'POST',
                    body: formData,
                    responseHandler: async (response) => {
                        if (!response.ok) {
                            const error = await response.text()
                            throw new Error(error)
                        }
                        const blob = await response.blob()
                        const fileName = response.headers
                            .get('content-disposition')
                            ?.split('filename=')[1]
                            ?.replace(/"/g, '') || 'decrypted_file'
                        return { blob, fileName }
                    },
                }
            },
        }),

    }),
})

export const { useEncryptDataMutation, useDecryptDataMutation } = encryptionApiBack