import psutil
import PySimpleGUI as sg
from modBusComunication import Modbus
from modbusConfigManager import ModbusConfigManager
import serial.tools.list_ports
import tempfile
from subprocess import Popen, PIPE, STDOUT

# Add a touch of color
sg.theme('Dark Grey 10')

processoAntigo = ""

# Create settings obj
settings = sg.UserSettings()
output_reg = [0, 0, 0, 0, 0, 0, 0, 0]

# Create global vars
selected_port = ""
timeAfter = 0

execuntando = False

modbus = Modbus()


config_manager = ModbusConfigManager()
configuracoes_salvas = config_manager.carregar_configuracoes()

if configuracoes_salvas:
    output_reg = configuracoes_salvas.get('OUTPUT', output_reg)


def listar_portas_seriais():
    portas = list(serial.tools.list_ports.comports())
    nomes_portas = [porta.device for porta in portas]
    return nomes_portas


# All the stuff inside your window.
layout_l = [
    [sg.Text('Lista de Instruções: ')],
    [sg.Multiline(size=(50, 33), key='-CODE-')],
    [sg.Button('Executar'), sg.Button(
        'Parar', disabled=True, button_color=('white', '#800000')), sg.Button('Limpar')]
    # Botão "Parar" desabilitado inicialmente
]

layout_r = [
    [sg.Text('Porta Serial:'), sg.Combo(listar_portas_seriais(),
                                        key='-CHOOSE_PORT-', enable_events=True)],
    [sg.Text('Entradas: ')],
    [sg.Listbox([f'I1  --  {modbus.input_reg[7]}',
                 f'I2  --  {modbus.input_reg[6]}',
                 f'I3  --  {modbus.input_reg[5]} ',
                 f'I4  --  {modbus.input_reg[4]}',
                 f'I5  --  {modbus.input_reg[3]}',
                 f'I6  --  {modbus.input_reg[2]}',
                 f'I7  --  {modbus.input_reg[1]}',
                 f'I8  --  {modbus.input_reg[0]}'],
                no_scrollbar=True, enable_events=True, s=(25, 11), select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, key='-INPUT-')],
    [sg.Text('Saidas: ')],
    [sg.Listbox([f'O1  --  {output_reg[7]}',
                 f'O2  --  {output_reg[6]}',
                 f'O3  --  {output_reg[5]}',
                 f'O4  --  {output_reg[4]}',
                 f'O5  --  {output_reg[3]}',
                 f'O6  --  {output_reg[2]}',
                 f'O7  --  {output_reg[1]}',
                 f'O8  --  {output_reg[0]}'],
                no_scrollbar=True, enable_events=True, select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, s=(25, 11), key='-OUTPUT-')],
    [sg.Text('Tempo de varredura(ms): ')],
    [sg.Input(key='-TIMEREAD-', s=(12, 1)),
     sg.Button("Ok", enable_events=True, key="-OKBTN-")],
]

menu_def = [['File', ['Abrir', 'Salvar',]],
            ['Help', ['Sobre', 'Ajuda']],]

layout = [[sg.Menu(menu_def)],
          [sg.Text('Compilador CLP',  font='_22',
                   justification='c', expand_x=True)],
          [sg.Col(layout_l), sg.Col(layout_r)],
          ]


# Create the Window
window = sg.Window('Compilador CLP', layout)
code_list = []


def criarArquivo(codigoTexto):
    arquivoCodigo = tempfile.TemporaryFile(suffix='.pas')
    arquivoCodigo.write(codigoTexto.encode('utf-8'))
    arquivoCodigo.seek(0)
    return arquivoCodigo.name


while True:
    event, values = window.read()
    if event == sg.WIN_CLOSED or event == 'Cancelar':  # if user closes window or clicks cancel
        break

    elif event == '-CHOOSE_PORT-':
        selected_port = values['-CHOOSE_PORT-']
        if selected_port.upper() == 'COM1':
            sg.popup_error(
                'A porta COM1 não é válida. Por favor, escolha outra porta.', title='Erro')
        else:
            window['-INPUT-'].Update(
                [f'I1  --  {modbus.input_reg[7]}',
                 f'I2  --  {modbus.input_reg[6]}',
                 f'I3  --  {modbus.input_reg[5]}',
                 f'I4  --  {modbus.input_reg[4]}',
                 f'I5  --  {modbus.input_reg[3]}',
                 f'I6  --  {modbus.input_reg[2]}',
                 f'I7  --  {modbus.input_reg[1]}',
                 f'I8  --  {modbus.input_reg[0]}']
            )
        if selected_port and selected_port.upper() != 'COM1':
            sg.popup(
                f'Porta Selecionada: {selected_port}', title='Porta Selecionada')

    elif event == 'Ajuda':
        file = open("frontend/Help.txt")
        Helper = file.read()
        sg.popup_scrolled(Helper, title="Helper", font=(
            "Arial", 12), size=(55, 20))

    elif event == "Sobre":
        file = open("frontend/About.txt")
        About = file.read()
        sg.popup_scrolled(About, title="About", font=(
            "Arial", 12), size=(55, 20))

    elif event == 'Abrir':
        file_path = sg.popup_get_file(
            'Selecione um arquivo para abrir', file_types=(("Text Files", "*.txt"),))
        if file_path:
            with open(file_path, 'r') as file:
                code = file.read()
                window['-CODE-'].update(code)

    elif event == 'Salvar':
        code = values['-CODE-']
        if code.strip():  # Check if the code is not empty or contains only whitespace
            file_path = sg.popup_get_file('Salve o arquivo como', file_types=(
                ("Text Files", "*.txt"),), save_as=True)
            if file_path:
                with open(file_path, 'w') as file:
                    file.write(code)
        else:
            sg.popup_error(
                'Nenhum código para salvar. Digite seu código antes de salvar.', title='Erro')

    elif event == '-OKBTN-':
        try:
            timeAfter = int(values['-TIMEREAD-'])
        except ValueError:
            timeAfter = 0
            sg.popup_error(
                'Por favor, insira um valor válido para o tempo após a varredura.', title='Erro')

    elif event == 'Executar':

        if not selected_port or selected_port.upper() == 'COM1':
            sg.popup_error(
                'Por favor, selecione uma porta válida antes de executar o código.', title='Erro na Porta')
            continue  # Volta ao início do loop, impedindo a execução do código

        print("passou!")
        execuntando = True
        # if (execuntando):
        #     window['Executar'].Update(disabled=True)
        #     window['Parar'].Update(disabled=False)

     # if(window['-CHOOSE_PORT-'].get() == ""):
        #     continue

        # arquivoCodigo = tempfile.TemporaryFile(suffix='.pas', delete=False)
        # arquivoCodigo.write(str(window['-CODE-'].get()+'\n').encode('utf-8'))
        # arquivoCodigo.seek(0)
        # arquivoCodigo.close()

        # if(processoAntigo != ""):
        #     for processo_filho in psutil.Process(processoAntigo.pid).children():
        #         processo_filho.terminate()
        #         processoAntigo = ""

        # processoAntigo = Popen(["java", "-cp", "../backend/target/compilador-clp-1.0-SNAPSHOT-jar-with-dependencies.jar", "iftm.compilador.clp.App", arquivoCodigo.name], stdin=PIPE, stdout=PIPE, stderr=STDOUT)

        # primeiroLido = False
        # possivelErro = ""
        # for line in processoAntigo.stdout:
        #     if(not primeiroLido):
        #         primeiroLido = True
        #         print(line)
        #     else:
        #         possivelErro = line
        #         break

        # if(len(possivelErro) > 5):
        #     sg.popup(possivelErro.decode('utf-8', errors='replace'), title='Ocorreu um erro ao compilar')
        #     processoAntigo = ""
        modbus.get_instrument(selected_port)
        modbus.read_input_registers()
        # modbus.write_output_registers(output_reg)

        # Atualize o arquivo JSON com as informações atuais
        config_manager.atualizar_input_reg(modbus.input_reg)
window.close()
