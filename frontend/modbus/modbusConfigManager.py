import json


class ModbusConfigManager:
    def __init__(self):
        self.config_file = 'storage\communication.json'

    def carregar_configuracoes(self):
        try:
            with open(self.config_file, 'r') as arquivo_config:
                configuracoes = json.load(arquivo_config)
                return configuracoes
        except FileNotFoundError:
            return None

    def salvar_configuracoes(self, configuracoes):
        with open(self.config_file, 'w') as arquivo_config:
            configuracoes['INPUT'] = configuracoes['INPUT'][::-1]
            json.dump(configuracoes, arquivo_config, indent=4)

    def atualizar_input_reg(self, novo_input_reg):
        configuracoes = self.carregar_configuracoes()
        if configuracoes is not None:
            input_reg_atual = configuracoes.get('INPUT', [])
            if input_reg_atual != novo_input_reg:
                configuracoes['INPUT'] = novo_input_reg
                self.salvar_configuracoes(configuracoes)
