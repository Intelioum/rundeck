import {ExecutionOutputGetResponse} from 'ts-rundeck/dist/lib/models'
import {Rundeck, TokenCredentialProvider} from 'ts-rundeck'

export class ExecutionLog {
    client: Rundeck

    offset = '0'
    completed = false

    constructor(readonly id: string) {
        this.client = new Rundeck(new TokenCredentialProvider('VpP6MpnXs91fsmXbM3eylKbMp89WimlD'), {baseUri: 'http://ubuntu:4440'})
    }

    async getOutput(maxLines: number): Promise<ExecutionOutputGetResponse> {
        const res = await this.client.executionOutputGet(this.id, {offset: this.offset, maxlines: maxLines})
        this.offset = res.offset
        this.completed = res.completed
        return res
    }
}