# ConverterServer

Uma prova de conceito elaborada pela Logicalis Brasil.

Nesta PoC, estão sendo utilizados os seguintes elementos:

1. Redis

   Utilizado como repositório persistido para armazenar os usuários da aplicação.
      
2. SpringBoot

   Framework para as Apis, provendo autenticação, autorização, endpoints REST e documentação online via Swagger.
   
3. StarOffice ou LibreOffice

   O pacote StarOffice oferece uma ferramenta que permite, via linha de comando, converter alguns formatos de documentos e planilhas eletrônicas. Esta prova de conceito utiliza algumas destas possibilidades ao manipular formatos open document. O caminho da ferramenta _soffice_ deve ser atualizado no arquivo de configuração _config.yml_, descrito mais adiante.


##### Dependencies
* Linux cc tools (yum install gcc gcc-c++ autoconf automake)
* Tcl >= 8.5 (yum install tcl)
* Java >= 8 (yum install java-1.8.0-openjdk*)


## Conversões disponíveis

* XLS to DOC
* XLS to HTML
* XLS to TXT
* XLS to ODS
* CSV to ODS
* CSV to XLS
* CSV to HTML
* DOC to ODT

### Métodos

Conversão de documentos:

	POST https://dataprev.cps.br.swlogicalis.com/dataprevpoc/api/convert/<from>/<to>
	

### Configurando e Executando os serviços

Para a prova de conceito, além do _jar_ da aplicação, foi criado também o arquivo de configuração _config.yml_:


    # Converter settings
    converter:
      package: com.logicalis.br.dataprev.converters
      xls-to-ods: /opt/libreoffice5.4/program/soffice --headless --convert-to ods --outdir /tmp {0}
      csv-to-ods: /opt/libreoffice5.4/program/soffice --headless --convert-to ods --outdir /tmp {0}
      csv-to-xls: /opt/libreoffice5.4/program/soffice --headless --convert-to xls --outdir /tmp {0}
      csv-to-html: /opt/libreoffice5.4/program/soffice --headless --convert-to html --outdir /tmp {0}
      doc-to-odt: /opt/libreoffice5.4/program/soffice --headless --convert-to odt --outdir /tmp {0}
      html-header: |
            <html><head><style>
              td { border-right: 1px solid gray; border-bottom: 1px solid gray; }
              th { background-color: '#dddddd'; border-right: 1px solid gray; border-bottom: 1px solid gray; }
              tr { vertical-align: top; }
            </style></head><body>


Caso este arquivo não seja encontrado no mesmo diretório onde o serviço da API está sendo executado, será utilizado o arquivo incluido no _jar_ (mas que provavelmente irá referenciar o OpenOffice ou LibreOffice em outro local e portanto com erro).

Antes de iniciar o serviço, é necessário subir o redis:

    nohup redis-server > redis.log &
    
Em seguida, o servidor da API pode ser iniciado:

    nohup java -Xms2g -Xmx2g -jar SimpleDocumentConverter-0.0.1-SNAPSHOT.jar &
    
Os logs de execução podem ser monitorados: _sdc.log_ e _redis.log_. O primeiro traz os logs da engine da API.
