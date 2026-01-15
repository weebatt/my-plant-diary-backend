import { api } from '@/api/client'

type Plant = {
  id: string
  latinName: string
  commonName?: string
  light?: string | null
  water?: string | null
}

function extractItems(data: any): Plant[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data.items)) return data.items
  if (Array.isArray(data.content)) return data.content
  return []
}

export async function fetchDistinctLightWater(): Promise<{ light: string[]; water: string[] }> {
  const { data } = await api.get('/plants', { params: { page: 0, size: 200 } })
  const items = extractItems(data)
  const lights = new Set<string>()
  const waters = new Set<string>()
  for (const p of items) {
    const l = (p.light || '').trim().toLowerCase()
    if (l) lights.add(l)
    const w = (p.water || '').trim().toLowerCase()
    if (w) waters.add(w)
  }
  return { light: Array.from(lights).sort(), water: Array.from(waters).sort() }
}
