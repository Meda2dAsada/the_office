package office.sync;

import office.agents.AdminAgent;
import office.agents.ClientAgent;
import office.agents.DistributedBranchAgent;
import office.agents.ReceptionistAgent;
import office.agents.SellerAgent;
import office.agents.SupervisorAgent;
import office.model.SimulationConfig;

/**
 * Orquesta el inicio, creacion de agentes, generacion gradual de clientes y detencion segura.
 */
public class SimulationController {
    private final OfficeContext context;
    private DistributedBranchAgent distributedAgent;
    private Thread clientSpawnerThread;

    public SimulationController(SimulationConfig config) {
        this.context = new OfficeContext(config);
    }

    public OfficeContext getContext() {
        return context;
    }

    public void start() {
        SimulationConfig cfg = context.getConfig();
        String site = cfg.getDistributedMode() == SimulationConfig.DistributedMode.MATRIZ_SERVIDOR
                ? "Matriz"
                : "Sucursal";

        context.log("Iniciando simulacion en sede: " + site);

        // 1. Agente distribuido (Matriz servidor o Sucursal cliente socket)
        String distId = site.equals("Matriz") ? "Dist-Matriz" : "Dist-Sucursal";
        distributedAgent = new DistributedBranchAgent(context, distId, cfg.getDistributedMode(), cfg.getHost(), cfg.getPort());
        context.registerAgent(distributedAgent);
        distributedAgent.start();

        // 2. Empleados de la oficina
        for (int i = 1; i <= cfg.getReceptionistCount(); i++) {
            ReceptionistAgent r = new ReceptionistAgent(context, "R-" + i, site);
            context.registerAgent(r);
            r.start();
        }
        for (int i = 1; i <= cfg.getSellerCount(); i++) {
            SellerAgent s = new SellerAgent(context, "V-" + i, site);
            context.registerAgent(s);
            s.start();
        }
        for (int i = 1; i <= cfg.getSupervisorCount(); i++) {
            SupervisorAgent sup = new SupervisorAgent(context, "S-" + i, site);
            context.registerAgent(sup);
            sup.start();
        }
        for (int i = 1; i <= cfg.getAdminCount(); i++) {
            AdminAgent adm = new AdminAgent(context, "A-" + i, site);
            context.registerAgent(adm);
            adm.start();
        }

        // 3. Generador de clientes segun timer de llegada
        clientSpawnerThread = new Thread(() -> {
            try {
                for (int i = 0; i < cfg.getClientCount() && context.isRunning(); i++) {
                    context.checkPause();
                    spawnClient(site);
                    if (i < cfg.getClientCount() - 1) {
                        Thread.sleep(cfg.getClientArrivalIntervalMs());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Client-Spawner");
        clientSpawnerThread.setDaemon(true);
        clientSpawnerThread.start();
    }

    public void spawnClient(String site) {
        if (!context.isRunning()) return;
        int num = context.nextClientNumber();
        ClientAgent c = new ClientAgent(context, "C-" + num, site);
        context.registerAgent(c);
        c.start();
    }

    public void requestFirstSellerTransfer() {
        for (var agent : context.getAgents()) {
            if (agent instanceof SellerAgent seller && seller.isAlive()) {
                seller.requestTransfer();
                if (distributedAgent != null) {
                    distributedAgent.sendSellerTransfer(seller.getAgentId());
                }
                context.log("Solicitado traslado de " + seller.getAgentId() + " hacia otra sede.");
                return;
            }
        }
        context.log("No hay vendedores activos disponibles para trasladar.");
    }

    public void stop() {
        if (clientSpawnerThread != null) {
            clientSpawnerThread.interrupt();
        }
        context.stopAll();
        context.log("Simulacion detenida.");
    }
}
