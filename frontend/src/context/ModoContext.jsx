/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext } from 'react'

export const ModoContext = createContext('edicion')
export const ModoProvider = ModoContext.Provider
export const useModo = () => useContext(ModoContext)
export default ModoContext
