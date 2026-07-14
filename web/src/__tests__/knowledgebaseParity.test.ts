import { describe, expect, it } from 'vitest'
import androidKnowledge from '../../../app/src/main/assets/knowledgebase.json?raw'
import webKnowledge from '../../public/knowledgebase.json?raw'

describe('Knowledge Base asset parity', () => {
  it('serves the same local catalogue as Android', () => {
    expect(JSON.parse(webKnowledge)).toEqual(JSON.parse(androidKnowledge))
  })
})
